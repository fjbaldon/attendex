package com.github.fjbaldon.attendex.platform.event;

import com.github.fjbaldon.attendex.platform.attendee.AttendeeFacade;
import com.github.fjbaldon.attendex.platform.attendee.dto.AttendeeDto;
import com.github.fjbaldon.attendex.platform.capture.CaptureFacade;
import com.github.fjbaldon.attendex.platform.capture.dto.EntryDetailsDto;
import com.github.fjbaldon.attendex.platform.capture.dto.EventSyncDto;
import com.github.fjbaldon.attendex.platform.event.dto.*;
import com.github.fjbaldon.attendex.platform.event.events.EventCreatedEvent;
import com.github.fjbaldon.attendex.platform.event.events.RosterEntryAddedEvent;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventFacade {

    private final EventRepository eventRepository;
    private final RosterRepository rosterRepository;
    private final SessionRepository sessionRepository;
    private final AttendeeFacade attendeeFacade;
    private final CaptureFacade captureFacade;
    private final ApplicationEventPublisher eventPublisher;

    EventFacade(
            EventRepository eventRepository,
            RosterRepository rosterRepository,
            SessionRepository sessionRepository,
            AttendeeFacade attendeeFacade,
            @Lazy CaptureFacade captureFacade,
            ApplicationEventPublisher eventPublisher
    ) {
        this.eventRepository = eventRepository;
        this.rosterRepository = rosterRepository;
        this.sessionRepository = sessionRepository;
        this.attendeeFacade = attendeeFacade;
        this.captureFacade = captureFacade;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public EventDto createEvent(Long organizationId, Long organizerId, CreateEventRequestDto dto) {
        Event event = Event.create(
                organizationId,
                organizerId,
                dto.name(),
                dto.startDate(),
                dto.endDate(),
                dto.graceMinutesBefore(),
                dto.graceMinutesAfter()
        );

        dto.sessions().forEach(sessionDto -> {
            Session session = Session.create(sessionDto.activityName(), sessionDto.targetTime(), sessionDto.intent());
            event.addSession(session);
        });

        Event saved = eventRepository.save(event);
        eventPublisher.publishEvent(new EventCreatedEvent(saved.getId(), saved.getOrganizationId(), saved.getName()));

        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<EventDto> findEvents(Long organizationId, String query, Pageable pageable) {
        if (query != null && !query.isBlank()) {
            return eventRepository.searchByOrganizationId(organizationId, query.trim(), pageable)
                    .map(this::toDto);
        }
        return eventRepository.findAllByOrganizationId(organizationId, pageable)
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public EventDto findEventById(Long eventId, Long organizationId) {
        return eventRepository.findByIdAndOrganizationId(eventId, organizationId)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));
    }

    @Transactional
    public void addAttendeeToRoster(Long eventId, Long attendeeId, Long organizationId) {
        attendeeFacade.findAttendeeById(attendeeId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Attendee not found in this organization."));

        Event event = eventRepository.findByIdAndOrganizationId(eventId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        RosterEntryId id = new RosterEntryId(eventId, attendeeId);
        Assert.isTrue(!rosterRepository.existsById(id), "Attendee is already on the roster for this event.");

        RosterEntry rosterEntry = RosterEntry.create(event, attendeeId);
        rosterRepository.save(rosterEntry);

        eventPublisher.publishEvent(new RosterEntryAddedEvent(eventId, organizationId, attendeeId));
    }

    @Transactional
    public void removeAttendeeFromRoster(Long eventId, Long attendeeId) {
        RosterEntryId id = new RosterEntryId(eventId, attendeeId);
        rosterRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<AttendeeDto> findRosterForEvent(Long eventId, Long organizationId, Pageable pageable) {
        eventRepository.findByIdAndOrganizationId(eventId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        return rosterRepository.findAttendeeIdsByEventId(eventId, pageable)
                .map(attendeeId -> attendeeFacade.findAttendeeById(attendeeId, organizationId)
                        .orElseThrow(() -> new IllegalStateException("Roster data inconsistency: Attendee " + attendeeId + " not found")));
    }

    @Transactional(readOnly = true)
    public Page<EntryDetailsDto> findEntriesByIntent(Long eventId, Long organizationId, String intent, Pageable pageable) {
        List<Long> sessionIds = sessionRepository.findSessionIdsByEventIdAndIntent(eventId, intent);
        if (sessionIds.isEmpty()) {
            return Page.empty(pageable);
        }
        return captureFacade.findEntriesBySessionIds(organizationId, sessionIds, pageable);
    }

    @Transactional(readOnly = true)
    public Page<EventDto> findUpcomingEvents(Long organizationId, Pageable pageable) {
        return eventRepository.findByOrganizationIdAndStartDateAfterOrderByStartDateAsc(
                organizationId,
                Instant.now(),
                pageable
        ).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public List<EventForSyncDto> findActiveEventsForSync(Long organizationId) {
        Page<Event> events = eventRepository.findAllByOrganizationId(organizationId, Pageable.unpaged());
        return events.stream()
                .map(this::toEventForSyncDto)
                .collect(Collectors.toList());
    }

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

    @Transactional(readOnly = true)
    public List<SessionDetailsDto> findAllSessionsForEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        return event.getSessions().stream()
                .map(s -> new SessionDetailsDto(
                        s.getId(),
                        s.getTargetTime(),
                        event.getGraceMinutesBefore(),
                        event.getGraceMinutesAfter()
                ))
                .collect(Collectors.toList());
    }

    private EventDto toDto(Event event) {
        return new EventDto(
                event.getId(),
                event.getName(),
                event.getStartDate(),
                event.getEndDate(),
                event.getGraceMinutesBefore(),
                event.getGraceMinutesAfter(),
                event.getCreatedAt(),
                calculateEventStatus(event),
                event.getSessions().stream().map(this::toDto).collect(Collectors.toList())
        );
    }

    private String calculateEventStatus(Event event) {
        Instant now = Instant.now();
        if (now.isAfter(event.getEndDate())) {
            return "PAST";
        }
        if (now.isBefore(event.getStartDate())) {
            return "UPCOMING";
        }

        boolean isActive = event.getSessions().stream().anyMatch(session -> {
            Instant startWindow = session.getTargetTime().minus(event.getGraceMinutesBefore(), ChronoUnit.MINUTES);
            Instant endWindow = session.getTargetTime().plus(event.getGraceMinutesAfter(), ChronoUnit.MINUTES);
            return !now.isBefore(startWindow) && !now.isAfter(endWindow);
        });

        return isActive ? "ACTIVE" : "ONGOING";
    }

    private SessionDto toDto(Session session) {
        return new SessionDto(
                session.getActivityName(),
                session.getTargetTime(),
                session.getIntent()
        );
    }

    private EventForSyncDto toEventForSyncDto(Event event) {
        var sessions = event.getSessions().stream()
                .map(s -> new EventForSyncDto.SessionForSyncDto(s.getId(), s.getActivityName(), s.getTargetTime(), s.getIntent()))
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
