package com.github.fjbaldon.attendex.platform.capture;

import com.github.fjbaldon.attendex.platform.analytics.dto.EventStatsDto;
import com.github.fjbaldon.attendex.platform.attendee.AttendeeFacade;
import com.github.fjbaldon.attendex.platform.attendee.dto.AttendeeDto;
import com.github.fjbaldon.attendex.platform.capture.dto.BatchSyncResponse;
import com.github.fjbaldon.attendex.platform.capture.dto.EntryDetailsDto;
import com.github.fjbaldon.attendex.platform.capture.dto.EntrySyncRequestDto;
import com.github.fjbaldon.attendex.platform.capture.events.EntryCreatedEvent;
import com.github.fjbaldon.attendex.platform.event.EventFacade;
import com.github.fjbaldon.attendex.platform.event.dto.SessionDetailsDto;
import com.github.fjbaldon.attendex.platform.organization.OrganizationFacade;
import com.github.fjbaldon.attendex.platform.organization.dto.ScannerAuthDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CaptureFacade {

    private final EntryRepository entryRepository;
    private final AttendeeFacade attendeeFacade;
    private final OrganizationFacade organizationFacade;
    private final EventFacade eventFacade;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public BatchSyncResponse syncEntries(
            Long organizationId,
            String scannerEmail,
            EntrySyncRequestDto request
    ) {
        ScannerAuthDto scanner = organizationFacade.findScannerAuthByEmail(scannerEmail)
                .filter(s -> s.organizationId().equals(organizationId))
                .orElseThrow(() -> new EntityNotFoundException("Scanner not found."));

        Map<Long, List<SessionDetailsDto>> scheduleCache = new HashMap<>();

        List<String> failedUuids = new ArrayList<>();
        int successCount = 0;

        for (var record : request.records()) {
            try {
                processSingleEntry(organizationId, scanner.id(), record, scheduleCache);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to sync entry UUID: {}", record.scanUuid(), e);
                failedUuids.add(record.scanUuid());
            }
        }

        return new BatchSyncResponse(successCount, failedUuids.size(), failedUuids);
    }

    private SessionDetailsDto findBestSessionInMemory(List<SessionDetailsDto> sessions, Instant scanTime) {
        SessionDetailsDto bestMatch = null;
        double minWeightedDiff = Double.MAX_VALUE;
        long twelveHoursInSeconds = 12 * 60 * 60;

        for (SessionDetailsDto session : sessions) {
            long diff = Duration.between(session.targetTime(), scanTime).getSeconds();
            long absDiff = Math.abs(diff);

            if (absDiff <= twelveHoursInSeconds) {
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
        long minutesDifference = ChronoUnit.MINUTES.between(details.targetTime(), scanTimestamp);

        if (minutesDifference < -details.graceMinutesBefore()) {
            return "EARLY";
        } else if (minutesDifference > details.graceMinutesAfter()) {
            return "LATE";
        } else {
            return "PUNCTUAL";
        }
    }

    @Transactional(readOnly = true)
    public Page<EntryDetailsDto> findEntriesBySessionIds(Long organizationId, List<Long> sessionIds, Pageable pageable) {
        return entryRepository.findBySessionIdIn(sessionIds, pageable)
                .map(entry -> {
                    AttendeeDto attendee = attendeeFacade.findAttendeeById(entry.getAttendeeId(), organizationId)
                            .orElseThrow(() -> new IllegalStateException("Data inconsistency: Attendee not found"));

                    return new EntryDetailsDto(
                            entry.getId(),
                            entry.getScanTimestamp(),
                            entry.getPunctuality(),
                            attendee
                    );
                });
    }

    @Transactional(readOnly = true)
    public long countEntriesSince(Long organizationId, Instant timestamp) {
        return entryRepository.countByOrganizationIdAndSyncTimestampAfter(organizationId, timestamp);
    }

    @Transactional(readOnly = true)
    public List<EntryDetailsDto> findAllEntriesForEvent(Long organizationId, Long eventId) {
        eventFacade.findEventById(eventId, organizationId);

        List<Entry> entries = entryRepository.findByEventIdOrderByScanTimestampDesc(eventId);

        if (entries.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> attendeeIds = entries.stream()
                .map(Entry::getAttendeeId)
                .distinct()
                .toList();

        Map<Long, AttendeeDto> attendeeMap = attendeeFacade.findAttendeesByIds(attendeeIds)
                .stream()
                .collect(Collectors.toMap(AttendeeDto::id, Function.identity()));

        return entries.stream()
                .map(entry -> {
                    AttendeeDto attendee = attendeeMap.get(entry.getAttendeeId());

                    if (attendee == null) {
                        return null;
                    }

                    return new EntryDetailsDto(
                            entry.getId(),
                            entry.getScanTimestamp(),
                            entry.getPunctuality(),
                            attendee
                    );
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private void processSingleEntry(Long organizationId, Long scannerId, EntrySyncRequestDto.EntryRecord record, Map<Long, List<SessionDetailsDto>> scheduleCache) {
        if (entryRepository.existsByScanUuid(record.scanUuid())) {
            return;
        }

        List<SessionDetailsDto> sessions = scheduleCache.computeIfAbsent(
                record.eventId(),
                eventFacade::findAllSessionsForEvent
        );

        SessionDetailsDto bestSession = findBestSessionInMemory(sessions, record.scanTimestamp());

        Long sessionId = null;
        String punctuality = "UNSCHEDULED";

        if (bestSession != null) {
            sessionId = bestSession.sessionId();
            punctuality = calculatePunctuality(record.scanTimestamp(), bestSession);

            if (entryRepository.existsByAttendeeIdAndSessionId(record.attendeeId(), sessionId)) {
                log.info("Skipping duplicate entry for attendee {} in session {}", record.attendeeId(), sessionId);
                return;
            }
        }

        Entry entry = Entry.create(
                organizationId,
                record.eventId(),
                sessionId,
                record.attendeeId(),
                scannerId,
                record.scanTimestamp(),
                punctuality,
                record.scanUuid()
        );

        entryRepository.saveAndFlush(entry);

        eventPublisher.publishEvent(new EntryCreatedEvent(
                entry.getId(),
                record.eventId(),
                organizationId,
                entry.getScanTimestamp()
        ));
    }

    @Transactional(readOnly = true)
    public EventStatsDto getEventStats(Long organizationId, Long eventId) {
        eventFacade.findEventById(eventId, organizationId);

        long totalScans = entryRepository.countByEventId(eventId);
        long rosterCount = eventFacade.countRosterForEvent(eventId);
        double rate = (rosterCount > 0) ? ((double) totalScans / rosterCount) * 100 : 0.0;

        List<Object[]> sessionCounts = entryRepository.countByEventIdGroupBySessionId(eventId);
        Set<Long> sessionIds = sessionCounts.stream()
                .map(row -> (Long) row[0])
                .collect(Collectors.toSet());

        Map<Long, String> sessionNames = eventFacade.getSessionNamesByIds(sessionIds);

        List<EventStatsDto.StatItem> sessionStats = sessionCounts.stream()
                .map(row -> {
                    Long id = (Long) row[0];
                    long count = (long) row[1];
                    String name = sessionNames.getOrDefault(id, "Unknown Session");
                    return new EventStatsDto.StatItem(name, count);
                })
                .collect(Collectors.toList());

        List<Object[]> scannerCounts = entryRepository.countByEventIdGroupByScannerId(eventId);
        Set<Long> scannerIds = scannerCounts.stream()
                .map(row -> (Long) row[0])
                .collect(Collectors.toSet());

        Map<Long, String> scannerEmails = organizationFacade.getScannerEmailsByIds(scannerIds);

        List<EventStatsDto.StatItem> scannerStats = scannerCounts.stream()
                .map(row -> {
                    Long id = (Long) row[0];
                    long count = (long) row[1];
                    String email = scannerEmails.getOrDefault(id, "Unknown Scanner");
                    return new EventStatsDto.StatItem(email, count);
                })
                .collect(Collectors.toList());

        Instant first = entryRepository.findFirstScanByEventId(eventId).orElse(null);
        Instant last = entryRepository.findLastScanByEventId(eventId).orElse(null);

        return new EventStatsDto(totalScans, rosterCount, rate, first, last, sessionStats, scannerStats);
    }
}
