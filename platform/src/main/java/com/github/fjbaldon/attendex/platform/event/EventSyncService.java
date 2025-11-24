package com.github.fjbaldon.attendex.platform.event;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class EventSyncService {

    private final EventRepository eventRepository;
    private final RosterRepository rosterRepository;

    @Transactional(readOnly = true)
    public List<EventSyncDto> getEventsForSync(Long organizationId) {
        List<EventForSyncDto> events = findActiveEventsForSync(organizationId);

        return events.stream().map(e -> {
            List<EventSyncDto.SessionSyncDto> sessionSyncDtos = e.sessions().stream()
                    .map(s -> new EventSyncDto.SessionSyncDto(
                            s.id(),
                            s.activityName(),
                            s.targetTime(),
                            s.intent()
                    ))
                    .collect(Collectors.toList());

            return new EventSyncDto(
                    e.id(),
                    e.name(),
                    e.startDate(),
                    e.endDate(),
                    sessionSyncDtos,
                    // Roster is usually empty here as it's fetched progressively
                    List.of()
            );
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<EventSyncDto.RosterSyncDto> getFormattedRosterForSync(Long eventId, Long organizationId, Pageable pageable) {
        eventRepository.findByIdAndOrganizationId(eventId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        return rosterRepository.findRosterProjectionsByEventId(eventId, pageable)
                .map(proj -> new EventSyncDto.RosterSyncDto(
                        proj.attendeeId(),
                        proj.identity(),
                        proj.firstName(),
                        proj.lastName()
                ));
    }

    private List<EventForSyncDto> findActiveEventsForSync(Long organizationId) {
        Instant lookback = Instant.now().minus(24, ChronoUnit.HOURS);
        Instant lookahead = Instant.now().plus(7, ChronoUnit.DAYS);

        List<Event> events = eventRepository.findAllForSync(organizationId, lookback, lookahead);
        return events.stream()
                .map(this::toEventForSyncDto)
                .collect(Collectors.toList());
    }

    private EventForSyncDto toEventForSyncDto(Event event) {
        var sessions = event.getSessions().stream()
                .map(s -> new EventForSyncDto.SessionForSyncDto(
                        s.getId(),
                        s.getActivityName(),
                        s.getTargetTime(),
                        s.getIntent()))
                .toList();

        var roster = event.getRosterEntries().stream()
                .map(re -> new EventForSyncDto.RosterEntryForSyncDto(re.getId().getAttendeeId()))
                .toList();

        return new EventForSyncDto(
                event.getId(),
                event.getOrganizationId(),
                event.getName(),
                event.getStartDate(),
                event.getEndDate(),
                sessions,
                roster
        );
    }
}
