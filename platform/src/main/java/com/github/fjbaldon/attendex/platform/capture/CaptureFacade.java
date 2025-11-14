package com.github.fjbaldon.attendex.platform.capture;

import com.github.fjbaldon.attendex.platform.attendee.AttendeeFacade;
import com.github.fjbaldon.attendex.platform.capture.dto.EntrySyncRequestDto;
import com.github.fjbaldon.attendex.platform.capture.dto.EventSyncDto;
import com.github.fjbaldon.attendex.platform.capture.events.EntriesSyncedEvent;
import com.github.fjbaldon.attendex.platform.event.EventFacade;
import com.github.fjbaldon.attendex.platform.event.dto.EventForSyncDto;
import com.github.fjbaldon.attendex.platform.event.dto.SessionEventDto;
import com.github.fjbaldon.attendex.platform.organization.OrganizationFacade;
import com.github.fjbaldon.attendex.platform.organization.dto.ScannerAuthDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

        List<Entry> entriesToSave = new ArrayList<>();

        for (var record : request.records()) {
            if (!entryRepository.existsBySessionIdAndAttendeeId(record.sessionId(), record.attendeeId())) {
                String punctuality = "PUNCTUAL";
                Entry entry = Entry.create(record.sessionId(), record.attendeeId(), scanner.id(), record.scanTimestamp(), punctuality);
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

        Map<Long, List<Long>> attendeeIdsByEventId = entriesToSave.stream()
                .collect(Collectors.groupingBy(
                        entry -> sessionIdToEventIdMap.get(entry.getSessionId()),
                        Collectors.mapping(Entry::getAttendeeId, Collectors.toList())
                ));

        if (!attendeeIdsByEventId.isEmpty()) {
            eventPublisher.publishEvent(new EntriesSyncedEvent(attendeeIdsByEventId));
        }
    }

    @Transactional(readOnly = true)
    public long countEntriesSince(Long organizationId, Instant timestamp) {
        return 0;
    }

    @Transactional(readOnly = true)
    public long countEntriesByEventId(Long eventId) {
        return 0;
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
