package com.github.fjbaldon.attendex.platform.capture;

import com.github.fjbaldon.attendex.platform.attendee.AttendeeFacade;
import com.github.fjbaldon.attendex.platform.attendee.dto.AttendeeDto;
import com.github.fjbaldon.attendex.platform.capture.dto.EntryDetailsDto;
import com.github.fjbaldon.attendex.platform.capture.dto.EntrySyncRequestDto;
import com.github.fjbaldon.attendex.platform.capture.dto.EventSyncDto;
import com.github.fjbaldon.attendex.platform.capture.events.EntryCreatedEvent;
import com.github.fjbaldon.attendex.platform.event.EventFacade;
import com.github.fjbaldon.attendex.platform.event.dto.EventForSyncDto;
import com.github.fjbaldon.attendex.platform.event.dto.SessionDetailsDto;
import com.github.fjbaldon.attendex.platform.event.dto.SessionEventDto;
import com.github.fjbaldon.attendex.platform.organization.OrganizationFacade;
import com.github.fjbaldon.attendex.platform.organization.dto.ScannerAuthDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CaptureFacade {

    private final EntryRepository entryRepository;
    private final EventFacade eventFacade;
    private final AttendeeFacade attendeeFacade;
    private final OrganizationFacade organizationFacade;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<EventSyncDto> getEventsForSync(Long organizationId) {
        List<EventForSyncDto> events = eventFacade.findActiveEventsForSync(organizationId);
        return events.stream().map(this::toEventSyncDto).collect(Collectors.toList());
    }

    @Transactional
    public void syncEntries(Long organizationId, String scannerEmail, EntrySyncRequestDto request) {
        ScannerAuthDto scanner = organizationFacade.findScannerAuthByEmail(scannerEmail)
                .filter(s -> s.organizationId().equals(organizationId))
                .orElseThrow(() -> new EntityNotFoundException("Scanner not found or does not belong to this organization."));

        Set<Long> sessionIdsInRequest = request.records().stream()
                .map(EntrySyncRequestDto.EntryRecord::sessionId)
                .collect(Collectors.toSet());
        Map<Long, SessionDetailsDto> sessionDetailsMap = eventFacade.findSessionDetailsByIds(sessionIdsInRequest).stream()
                .collect(Collectors.toMap(SessionDetailsDto::sessionId, Function.identity()));

        List<Entry> entriesToSave = new ArrayList<>();
        for (var record : request.records()) {
            if (!entryRepository.existsBySessionIdAndAttendeeId(record.sessionId(), record.attendeeId())) {
                SessionDetailsDto details = sessionDetailsMap.get(record.sessionId());
                if (details == null) {
                    continue;
                }

                String punctuality = calculatePunctuality(record.scanTimestamp(), details);
                Entry entry = Entry.create(organizationId, record.sessionId(), record.attendeeId(), scanner.id(), record.scanTimestamp(), punctuality);
                entriesToSave.add(entry);
            }
        }

        if (entriesToSave.isEmpty()) {
            return;
        }

        entryRepository.saveAll(entriesToSave);

        Set<Long> sessionIds = entriesToSave.stream()
                .map(Entry::getSessionId)
                .collect(Collectors.toSet());
        Map<Long, Long> sessionIdToEventIdMap = eventFacade.findEventsForSessionIds(sessionIds).stream()
                .collect(Collectors.toMap(SessionEventDto::sessionId, SessionEventDto::eventId));

        for (Entry entry : entriesToSave) {
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
    public long countEntriesSince(Long organizationId, Instant timestamp) {
        return entryRepository.countByOrganizationIdAndSyncTimestampAfter(organizationId, timestamp);
    }

    @Transactional(readOnly = true)
    public long countEntriesByEventId(Long eventId) {
        return 0;
    }

    @Transactional(readOnly = true)
    public Page<EntryDetailsDto> findEntriesBySessionIds(Long organizationId, List<Long> sessionIds, Pageable pageable) {
        return entryRepository.findBySessionIdIn(sessionIds, pageable)
                .map(entry -> {
                    // This cross-module call inside a map is not ideal for performance at massive scale,
                    // but it is architecturally correct and sufficient for our needs.
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

    private EventSyncDto toEventSyncDto(EventForSyncDto event) {
        var sessions = event.sessions().stream()
                .map(s -> new EventSyncDto.SessionSyncDto(s.id(), s.activityName(), s.targetTime(), s.intent()))
                .toList();

        var roster = event.rosterEntries().stream()
                .map(re -> {
                    var attendee = attendeeFacade.findAttendeeById(re.attendeeId(), event.organizationId()).orElse(null);
                    return new EventSyncDto.RosterSyncDto(
                            re.attendeeId(),
                            attendee != null ? attendee.identity() : "N/A",
                            attendee != null ? attendee.firstName() : "N/A",
                            attendee != null ? attendee.lastName() : "N/A",
                            re.qrCodeHash()
                    );
                })
                .toList();

        return new EventSyncDto(event.id(), event.name(), sessions, roster);
    }
}
