package com.github.fjbaldon.attendex.platform.capture;

import com.github.fjbaldon.attendex.platform.attendee.AttendeeFacade;
import com.github.fjbaldon.attendex.platform.attendee.dto.AttendeeDto;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CaptureFacade {

    private final EntryRepository entryRepository;
    private final AttendeeFacade attendeeFacade;
    private final OrganizationFacade organizationFacade;
    private final EventFacade eventFacade; // Use Facade instead of Repo
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void syncEntries(
            Long organizationId,
            String scannerEmail,
            EntrySyncRequestDto request
    ) {
        ScannerAuthDto scanner = organizationFacade.findScannerAuthByEmail(scannerEmail)
                .filter(s -> s.organizationId().equals(organizationId))
                .orElseThrow(() -> new EntityNotFoundException("Scanner not found."));

        for (var record : request.records()) {
            // 1. Idempotency Check
            if (entryRepository.existsByScanUuid(record.scanUuid())) {
                continue; // Already processed, skip
            }

            // 2. Find Session based on Time (The "Thin Client" Logic)
            Optional<SessionDetailsDto> sessionOpt = eventFacade.findActiveSession(record.eventId(), record.scanTimestamp());

            Long sessionId = null;
            String punctuality = "UNSCHEDULED"; // Default if no session matches

            if (sessionOpt.isPresent()) {
                SessionDetailsDto session = sessionOpt.get();
                sessionId = session.sessionId();
                punctuality = calculatePunctuality(record.scanTimestamp(), session);
            } else {
                // OPTIONAL: Logic for "Late" vs "Unscheduled"
                // If we can't find an active session, maybe find the *nearest* session
                // and mark as LATE? For now, we'll just log it as UNSCHEDULED.
            }

            Entry entry = Entry.create(
                    organizationId,
                    sessionId,
                    record.attendeeId(),
                    scanner.id(),
                    record.scanTimestamp(),
                    punctuality,
                    record.scanUuid()
            );

            try {
                // Save individually to isolate failures
                entryRepository.saveAndFlush(entry);

                // Publish event for Analytics
                eventPublisher.publishEvent(new EntryCreatedEvent(
                        entry.getId(),
                        record.eventId(),
                        organizationId,
                        entry.getScanTimestamp()
                ));

            } catch (DataIntegrityViolationException e) {
                // Swallow duplicate UUID errors that slipped past the check
                log.warn("Duplicate entry ignored: {}", record.scanUuid());
            }
        }
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
}
