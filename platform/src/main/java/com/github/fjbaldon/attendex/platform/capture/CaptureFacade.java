package com.github.fjbaldon.attendex.platform.capture;

import com.github.fjbaldon.attendex.platform.attendee.AttendeeFacade;
import com.github.fjbaldon.attendex.platform.attendee.dto.AttendeeDto;
import com.github.fjbaldon.attendex.platform.capture.dto.EntryDetailsDto;
import com.github.fjbaldon.attendex.platform.capture.dto.EntrySyncRequestDto;
import com.github.fjbaldon.attendex.platform.capture.events.EntryCreatedEvent;
import com.github.fjbaldon.attendex.platform.event.dto.SessionDetailsDto;
import com.github.fjbaldon.attendex.platform.organization.OrganizationFacade;
import com.github.fjbaldon.attendex.platform.organization.dto.ScannerAuthDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CaptureFacade {

    private final EntryRepository entryRepository;
    private final AttendeeFacade attendeeFacade;
    private final OrganizationFacade organizationFacade;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void syncEntries(
            Long organizationId,
            String scannerEmail,
            EntrySyncRequestDto request,
            Map<Long, SessionDetailsDto> sessionDetailsMap,
            Map<Long, Long> sessionIdToEventIdMap
    ) {
        ScannerAuthDto scanner = organizationFacade.findScannerAuthByEmail(scannerEmail)
                .filter(s -> s.organizationId().equals(organizationId))
                .orElseThrow(() -> new EntityNotFoundException("Scanner not found or does not belong to this organization."));

        List<Entry> newEntries = new ArrayList<>();

        for (var record : request.records()) {
            if (!entryRepository.existsBySessionIdAndAttendeeId(record.sessionId(), record.attendeeId())) {
                SessionDetailsDto details = sessionDetailsMap.get(record.sessionId());
                if (details == null) {
                    continue;
                }
                String punctuality = calculatePunctuality(record.scanTimestamp(), details);
                Entry entry = Entry.create(organizationId, record.sessionId(), record.attendeeId(), scanner.id(), record.scanTimestamp(), punctuality);

                try {
                    entryRepository.saveAndFlush(entry);
                    newEntries.add(entry);
                } catch (DataIntegrityViolationException _) {
                }
            }
        }

        for (Entry entry : newEntries) {
            Long eventId = sessionIdToEventIdMap.get(entry.getSessionId());
            if (eventId != null) {
                eventPublisher.publishEvent(new EntryCreatedEvent(
                        entry.getId(),
                        eventId,
                        organizationId,
                        entry.getScanTimestamp()
                ));
            }
        }
    }

    @Transactional(readOnly = true)
    public Page<EntryDetailsDto> findEntriesBySessionIds(Long organizationId, List<Long> sessionIds, Pageable pageable) {
        return entryRepository.findBySessionIdIn(sessionIds, pageable)
                .map(entry -> {
                    AttendeeDto attendee = attendeeFacade.findAttendeeById(entry.getAttendeeId(), organizationId)
                            .orElseThrow(() -> new IllegalStateException("Data inconsistency: Attendee not found for entry"));

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
}
