package com.github.fjbaldon.attendex.platform.event;

import com.github.fjbaldon.attendex.platform.attendee.AttendeeFacade;
import com.github.fjbaldon.attendex.platform.attendee.dto.AttendeeDto;
import com.github.fjbaldon.attendex.platform.event.dto.EventDto;
import com.github.fjbaldon.attendex.platform.event.dto.EventForSyncDto;
import com.github.fjbaldon.attendex.platform.event.dto.SessionEventDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventFacade {

    private final EventRepository eventRepository;
    private final RosterRepository rosterRepository;
    private final AttendeeFacade attendeeFacade;
    private final SessionRepository sessionRepository;

    @Transactional
    public void addAttendeeToRoster(Long eventId, Long attendeeId, Long organizationId) {
        attendeeFacade.findAttendeeById(attendeeId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Attendee not found in this organization."));

        Event event = eventRepository.findByIdAndOrganizationId(eventId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        RosterEntryId id = new RosterEntryId(eventId, attendeeId);
        Assert.isTrue(!rosterRepository.existsById(id), "Attendee is already on the roster for this event.");

        String qrCodeHash = UUID.randomUUID().toString();
        RosterEntry rosterEntry = RosterEntry.create(event, attendeeId, qrCodeHash);
        rosterRepository.save(rosterEntry);
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
    public List<EventForSyncDto> findActiveEventsForSync(Long organizationId) {
        Page<Event> events = eventRepository.findAllByOrganizationId(organizationId, Pageable.unpaged());
        return events.stream()
                .map(this::toEventForSyncDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countEvents(Long organizationId) {
        return eventRepository.count();
    }

    @Transactional(readOnly = true)
    public Page<EventDto> findUpcomingEvents(Long organizationId, Pageable pageable) {
        return Page.empty();
    }

    @Transactional(readOnly = true)
    public Page<EventDto> findRecentEvents(Long organizationId, Pageable pageable) {
        return Page.empty();
    }

    @Transactional(readOnly = true)
    public long countRosterByEventId(Long eventId) {
        return rosterRepository.count();
    }

    @Transactional(readOnly = true)
    public List<SessionEventDto> findEventsForSessionIds(Set<Long> sessionIds) {
        return sessionRepository.findSessionsWithEventByIdIn(sessionIds).stream()
                .map(session -> new SessionEventDto(session.getId(), session.getEvent().getId()))
                .collect(Collectors.toList());
    }

    private EventForSyncDto toEventForSyncDto(Event event) {
        var sessions = event.getSessions().stream()
                .map(s -> new EventForSyncDto.SessionForSyncDto(s.getId(), s.getActivityName(), s.getTargetTime(), s.getIntent()))
                .toList();

        var roster = event.getRosterEntries().stream()
                .map(re -> new EventForSyncDto.RosterEntryForSyncDto(re.getId().getAttendeeId(), re.getQrCodeHash()))
                .toList();

        return new EventForSyncDto(event.getId(), event.getOrganizationId(), event.getName(), sessions, roster);
    }
}
