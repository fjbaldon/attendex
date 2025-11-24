package com.github.fjbaldon.attendex.platform.event;

import com.github.fjbaldon.attendex.platform.attendee.AttendeeFacade;
import com.github.fjbaldon.attendex.platform.attendee.AttendeeDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class RosterService {

    private final RosterRepository rosterRepository;
    private final EventRepository eventRepository;
    private final AttendeeFacade attendeeFacade;
    private final ApplicationEventPublisher eventPublisher;

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

    @Transactional
    public int bulkAddAttendeesByCriteria(Long eventId, Long organizationId, BulkAddCriteriaRequestDto criteria) {
        Event event = eventRepository.findByIdAndOrganizationId(eventId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        List<Long> matchingAttendeeIds = attendeeFacade.findIdsByCriteria(
                organizationId,
                criteria.query(),
                criteria.attributes()
        );

        if (matchingAttendeeIds.isEmpty()) {
            return 0;
        }

        Set<Long> existingAttendeeIds = rosterRepository.findAllAttendeeIdsByEventId(eventId);

        List<RosterEntry> newEntries = matchingAttendeeIds.stream()
                .filter(id -> !existingAttendeeIds.contains(id))
                .map(id -> RosterEntry.create(event, id))
                .collect(Collectors.toList());

        if (!newEntries.isEmpty()) {
            rosterRepository.saveAll(newEntries);
        }

        return newEntries.size();
    }

    @Transactional(readOnly = true)
    public Page<AttendeeDto> findRosterForEvent(Long eventId, Long organizationId, String query, Pageable pageable) {
        eventRepository.findByIdAndOrganizationId(eventId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        Page<Long> attendeeIds;

        if (query != null && !query.isBlank()) {
            attendeeIds = rosterRepository.searchAttendeeIdsByEventId(eventId, query.trim(), pageable);
        } else {
            attendeeIds = rosterRepository.findAttendeeIdsByEventId(eventId, pageable);
        }

        return attendeeIds.map(attendeeId -> attendeeFacade.findAttendeeById(attendeeId, organizationId)
                .orElseGet(() -> new AttendeeDto(
                        attendeeId,
                        "DELETED",
                        "Deleted",
                        "Attendee",
                        Collections.emptyMap(),
                        null
                )));
    }

    @Transactional(readOnly = true)
    public long countRosterForEvent(Long eventId) {
        return rosterRepository.countByIdEventId(eventId);
    }

    @Transactional(readOnly = true)
    public List<EventDto> findEventsForAttendee(Long attendeeId) {
        return rosterRepository.findEventsByAttendeeId(attendeeId).stream()
                .map(this::toDto)
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
                event.calculateStatus(Instant.now()), // Uses entity logic
                event.getSessions().stream().map(this::toSessionDto).collect(Collectors.toList())
        );
    }

    private SessionDto toSessionDto(Session session) {
        return new SessionDto(
                session.getId(),
                session.getActivityName(),
                session.getTargetTime(),
                session.getIntent()
        );
    }
}
