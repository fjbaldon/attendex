package com.github.fjbaldon.attendex.platform.capture;

import com.github.fjbaldon.attendex.platform.attendee.AttendeeDto;
import com.github.fjbaldon.attendex.platform.attendee.AttendeeFacade;
import com.github.fjbaldon.attendex.platform.event.EventFacade;
import com.github.fjbaldon.attendex.platform.event.SessionDetailsDto;
import com.github.fjbaldon.attendex.platform.organization.OrganizationFacade;
import com.github.fjbaldon.attendex.platform.organization.ScannerAuthDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Slf4j
class CaptureIngestService {

    private final EntryRepository entryRepository;
    private final OrganizationFacade organizationFacade;
    private final EventFacade eventFacade;
    private final AttendeeFacade attendeeFacade;
    private final OrphanService orphanService;
    private final ApplicationEventPublisher eventPublisher;

    public CaptureIngestService(
            EntryRepository entryRepository,
            OrganizationFacade organizationFacade,
            EventFacade eventFacade,
            AttendeeFacade attendeeFacade,
            OrphanService orphanService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.entryRepository = entryRepository;
        this.organizationFacade = organizationFacade;
        this.eventFacade = eventFacade;
        this.attendeeFacade = attendeeFacade;
        this.orphanService = orphanService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public BatchSyncResponse syncEntries(Long organizationId, String scannerEmail, EntrySyncRequestDto request) {
        ScannerAuthDto scanner = organizationFacade.findScannerAuthByEmail(scannerEmail)
                .filter(s -> s.organizationId().equals(organizationId))
                .orElseThrow(() -> new EntityNotFoundException("Scanner not found."));

        Map<Long, List<SessionDetailsDto>> scheduleCache = new HashMap<>();
        List<String> failedUuids = new ArrayList<>();
        Map<Long, List<Long>> syncedAttendees = new HashMap<>();
        int successCount = 0;

        // 1. Pre-filter known UUIDs (Optimization)
        List<String> allUuids = request.records().stream()
                .map(EntrySyncRequestDto.EntryRecord::scanUuid)
                .toList();
        Set<String> existingUuids = new HashSet<>(entryRepository.findExistingScanUuids(allUuids));

        for (var record : request.records()) {
            if (existingUuids.contains(record.scanUuid())) {
                successCount++;
                continue;
            }

            try {
                boolean created = processSingleEntry(organizationId, scanner.id(), record, scheduleCache);
                if (created) {
                    successCount++;
                    syncedAttendees
                            .computeIfAbsent(record.eventId(), k -> new ArrayList<>())
                            .add(record.attendeeId());
                }
            } catch (EntityNotFoundException e) {
                // Logic Error (Missing Event/Attendee) -> Quarantine
                log.warn("Orphaned Entry (Entity Not Found): {}", e.getMessage());
                orphanService.saveToQuarantine(organizationId, record, e.getMessage());
                successCount++; // We count it as processed so mobile stops retrying
            } catch (DataIntegrityViolationException e) {
                // FIXED: Distinguish between Duplicate UUID (Success) and Data Error (Orphan)
                if (e.getMessage() != null && e.getMessage().contains("capture_entry_scan_uuid_key")) {
                    log.info("Race condition caught for UUID {}. Treated as idempotent success.", record.scanUuid());
                } else {
                    log.error("Data Integrity Error for UUID {}: {}", record.scanUuid(), e.getMessage());
                    orphanService.saveToQuarantine(organizationId, record, "Data Integrity: " + e.getMostSpecificCause().getMessage());
                }
                successCount++;
            } catch (Exception e) {
                // Unknown System Error -> Fail and allow Mobile to retry
                log.error("Failed to sync UUID: {}", record.scanUuid(), e);
                failedUuids.add(record.scanUuid());
            }
        }

        if (!syncedAttendees.isEmpty()) {
            eventPublisher.publishEvent(new EntriesSyncedEvent(syncedAttendees));
        }

        return new BatchSyncResponse(successCount, failedUuids.size(), failedUuids);
    }

    @Transactional
    public boolean processSingleEntry(Long organizationId, Long scannerId, EntrySyncRequestDto.EntryRecord record, Map<Long, List<SessionDetailsDto>> scheduleCache) {
        if (!scheduleCache.containsKey(record.eventId()) && !eventFacade.exists(record.eventId())) {
            throw new EntityNotFoundException("Event not found: " + record.eventId());
        }

        List<SessionDetailsDto> sessions = scheduleCache.computeIfAbsent(record.eventId(), eventFacade::findAllSessionsForEvent);
        SessionDetailsDto bestSession = findBestSessionInMemory(sessions, record.scanTimestamp());

        Long sessionId = null;
        String punctuality = "UNSCHEDULED";

        if (bestSession != null) {
            sessionId = bestSession.sessionId();
            punctuality = calculatePunctuality(record.scanTimestamp(), bestSession);
            if (entryRepository.existsByAttendeeIdAndSessionId(record.attendeeId(), sessionId)) {
                return false; // Duplicate check (Session)
            }
        } else {
            // FIXED: Prevent spamming Unscheduled entries.
            // If they already have an Unscheduled entry for this event, ignore this one.
            if (entryRepository.existsByAttendeeIdAndEventIdAndSessionIdIsNull(record.attendeeId(), record.eventId())) {
                return false; // Duplicate check (Unscheduled)
            }
        }

        Map<String, Object> snapshotAttributes = attendeeFacade.findAttendeeById(record.attendeeId(), organizationId)
                .map(AttendeeDto::attributes)
                .orElse(Collections.emptyMap());

        Entry entry = Entry.create(
                organizationId,
                record.eventId(),
                sessionId,
                record.attendeeId(),
                scannerId,
                record.scanTimestamp(),
                punctuality,
                record.scanUuid(),
                record.snapshotIdentity(),
                record.snapshotFirstName(),
                record.snapshotLastName(),
                snapshotAttributes
        );

        entryRepository.save(entry);

        eventPublisher.publishEvent(new EntryCreatedEvent(
                entry.getId(),
                record.eventId(),
                organizationId,
                sessionId,
                scannerId,
                entry.getScanTimestamp()
        ));

        return true;
    }

    @Transactional
    public void recalculateSessionPunctuality(Long sessionId, Instant targetTime, int graceBefore, int graceAfter) {
        log.info("Recalculating punctuality for session {} (Target: {}, Grace: -{}/+{})", sessionId, targetTime, graceBefore, graceAfter);
        entryRepository.recalculatePunctualityForSession(sessionId, targetTime, graceBefore, graceAfter);
    }

    @Transactional
    public void reassignEntriesForDeletedSession(Long sessionIdToDelete, List<SessionDetailsDto> remainingSessions) {
        List<Entry> entries = entryRepository.findBySessionIdsAndQuery(List.of(sessionIdToDelete), null, Pageable.unpaged()).getContent();

        if (entries.isEmpty()) return;

        log.info("Re-binning {} entries from deleted session {}", entries.size(), sessionIdToDelete);

        for (Entry entry : entries) {
            SessionDetailsDto bestMatch = findBestSessionInMemory(remainingSessions, entry.getScanTimestamp());

            if (bestMatch != null) {
                String newPunctuality = calculatePunctuality(entry.getScanTimestamp(), bestMatch);
                entry.reassignToSession(bestMatch.sessionId(), newPunctuality);
            } else {
                entry.reassignToSession(null, "UNSCHEDULED");
            }
        }

        entryRepository.saveAll(entries);
    }

    private SessionDetailsDto findBestSessionInMemory(List<SessionDetailsDto> sessions, Instant scanTime) {
        SessionDetailsDto bestMatch = null;
        double minWeightedDiff = Double.MAX_VALUE;
        for (SessionDetailsDto session : sessions) {
            long diff = Duration.between(session.targetTime(), scanTime).getSeconds();
            long absDiff = Math.abs(diff);

            if (absDiff <= 14400) {
                double weightedDiff = (diff >= 0) ? absDiff : (absDiff * 1.5);
                if (weightedDiff < minWeightedDiff) {
                    minWeightedDiff = weightedDiff;
                    bestMatch = session;
                }
            }
        }
        return bestMatch;
    }

    private String calculatePunctuality(Instant scanTimestamp, SessionDetailsDto details) {
        long minutesDiff = ChronoUnit.MINUTES.between(details.targetTime(), scanTimestamp);
        if (minutesDiff < -details.graceMinutesBefore()) return "EARLY";
        if (minutesDiff > details.graceMinutesAfter()) return "LATE";
        return "PUNCTUAL";
    }
}
