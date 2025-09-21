package com.github.fjbaldon.attendex.backend.service;

import com.github.fjbaldon.attendex.backend.dto.AttendeeResponse;
import com.github.fjbaldon.attendex.backend.dto.EventRequest;
import com.github.fjbaldon.attendex.backend.dto.EventResponse;
import com.github.fjbaldon.attendex.backend.mapper.AttendeeMapper;
import com.github.fjbaldon.attendex.backend.mapper.EventMapper;
import com.github.fjbaldon.attendex.backend.model.Attendee;
import com.github.fjbaldon.attendex.backend.model.Event;
import com.github.fjbaldon.attendex.backend.model.EventAttendee;
import com.github.fjbaldon.attendex.backend.model.Organizer;
import com.github.fjbaldon.attendex.backend.repository.AttendeeRepository;
import com.github.fjbaldon.attendex.backend.repository.EventAttendeeRepository;
import com.github.fjbaldon.attendex.backend.repository.EventRepository;
import com.github.fjbaldon.attendex.backend.repository.OrganizerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final OrganizerRepository organizerRepository;
    private final AttendeeRepository attendeeRepository;
    private final EventAttendeeRepository eventAttendeeRepository;
    private final EventMapper eventMapper;
    private final AttendeeMapper attendeeMapper;

    @Transactional
    public EventResponse createEvent(EventRequest request, String organizerUsername) {
        Organizer organizer = findOrganizerByUsername(organizerUsername);
        Event event = Event.builder()
                .eventName(request.getEventName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .organizer(organizer)
                .build();
        Event savedEvent = eventRepository.save(event);
        return eventMapper.toEventResponse(savedEvent);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getAllEventsByOrganizer(String organizerUsername) {
        Organizer organizer = findOrganizerByUsername(organizerUsername);
        return organizer.getEvents().stream()
                .map(eventMapper::toEventResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventResponse getEventById(Long eventId, String organizerUsername) {
        Event event = findEventById(eventId);
        validateEventOwnership(event, organizerUsername);
        return eventMapper.toEventResponse(event);
    }

    @Transactional
    public EventResponse updateEvent(Long eventId, EventRequest request, String organizerUsername) {
        Event event = findEventById(eventId);
        validateEventOwnership(event, organizerUsername);

        event.setEventName(request.getEventName());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());

        Event updatedEvent = eventRepository.save(event);
        return eventMapper.toEventResponse(updatedEvent);
    }

    @Transactional
    public void deleteEvent(Long eventId, String organizerUsername) {
        Event event = findEventById(eventId);
        validateEventOwnership(event, organizerUsername);
        eventRepository.delete(event);
    }

    private Organizer findOrganizerByUsername(String username) {
        return organizerRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Organizer not found"));
    }

    private Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with ID " + eventId + " not found"));
    }

    private void validateEventOwnership(Event event, String username) {
        if (!event.getOrganizer().getUsername().equals(username)) {
            throw new AccessDeniedException("You do not have permission to access this event");
        }
    }

    @Transactional
    public void addAttendeeToEvent(Long eventId, Long attendeeId, String organizerUsername) {
        Event event = findEventById(eventId);
        validateEventOwnership(event, organizerUsername);

        Attendee attendee = attendeeRepository.findById(attendeeId)
                .orElseThrow(() -> new EntityNotFoundException("Attendee with ID " + attendeeId + " not found"));

        EventAttendee eventAttendee = EventAttendee.builder()
                .event(event)
                .attendee(attendee)
                .build();

        eventAttendeeRepository.save(eventAttendee);
    }

    @Transactional
    public void removeAttendeeFromEvent(Long eventId, Long attendeeId, String organizerUsername) {
        Event event = findEventById(eventId);
        validateEventOwnership(event, organizerUsername);
        eventAttendeeRepository.deleteByEventIdAndAttendeeId(eventId, attendeeId);
    }

    @Transactional(readOnly = true)
    public List<AttendeeResponse> getAttendeesForEvent(Long eventId, String organizerUsername) {
        Event event = findEventById(eventId);
        validateEventOwnership(event, organizerUsername);

        return event.getEventAttendees().stream()
                .map(registration -> attendeeMapper.toAttendeeResponse(registration.getAttendee()))
                .collect(Collectors.toList());
    }
}
