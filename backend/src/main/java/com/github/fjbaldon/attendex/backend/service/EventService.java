package com.github.fjbaldon.attendex.backend.service;

import com.github.fjbaldon.attendex.backend.dto.AttendeeResponse;
import com.github.fjbaldon.attendex.backend.dto.EventRequest;
import com.github.fjbaldon.attendex.backend.dto.EventResponse;
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

    @Transactional
    public EventResponse createEvent(EventRequest request, String organizerEmail) {
        Organizer organizer = findOrganizerByEmail(organizerEmail);
        Event event = Event.builder()
                .eventName(request.getEventName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .organizer(organizer)
                .build();
        Event savedEvent = eventRepository.save(event);
        return toEventResponse(savedEvent);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getAllEventsByOrganizer(String organizerEmail) {
        Organizer organizer = findOrganizerByEmail(organizerEmail);
        return organizer.getEvents().stream()
                .map(this::toEventResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventResponse getEventById(Long eventId, String organizerEmail) {
        Event event = findEventById(eventId);
        validateEventOwnership(event, organizerEmail);
        return toEventResponse(event);
    }

    @Transactional
    public EventResponse updateEvent(Long eventId, EventRequest request, String organizerEmail) {
        Event event = findEventById(eventId);
        validateEventOwnership(event, organizerEmail);

        event.setEventName(request.getEventName());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());

        Event updatedEvent = eventRepository.save(event);
        return toEventResponse(updatedEvent);
    }

    @Transactional
    public void deleteEvent(Long eventId, String organizerEmail) {
        Event event = findEventById(eventId);
        validateEventOwnership(event, organizerEmail);
        eventRepository.delete(event);
    }

    private Organizer findOrganizerByEmail(String email) {
        return organizerRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Organizer not found"));
    }

    private Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with ID " + eventId + " not found"));
    }

    private void validateEventOwnership(Event event, String email) {
        if (!event.getOrganizer().getEmail().equals(email)) {
            throw new AccessDeniedException("You do not have permission to access this event");
        }
    }

    @Transactional
    public void addAttendeeToEvent(Long eventId, Long attendeeId, String organizerEmail) {
        Event event = findEventById(eventId);
        validateEventOwnership(event, organizerEmail);

        Attendee attendee = attendeeRepository.findById(attendeeId)
                .orElseThrow(() -> new EntityNotFoundException("Attendee with ID " + attendeeId + " not found"));

        EventAttendee eventAttendee = EventAttendee.builder()
                .event(event)
                .attendee(attendee)
                .build();

        eventAttendeeRepository.save(eventAttendee);
    }

    @Transactional
    public void removeAttendeeFromEvent(Long eventId, Long attendeeId, String organizerEmail) {
        Event event = findEventById(eventId);
        validateEventOwnership(event, organizerEmail);
        eventAttendeeRepository.deleteByEventIdAndAttendeeId(eventId, attendeeId);
    }

    @Transactional(readOnly = true)
    public List<AttendeeResponse> getAttendeesForEvent(Long eventId, String organizerEmail) {
        Event event = findEventById(eventId);
        validateEventOwnership(event, organizerEmail);

        return event.getEventAttendees().stream()
                .map(registration -> toAttendeeResponse(registration.getAttendee()))
                .collect(Collectors.toList());
    }

    private EventResponse toEventResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .eventName(event.getEventName())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .build();
    }

    private AttendeeResponse toAttendeeResponse(Attendee attendee) {
        return AttendeeResponse.builder()
                .id(attendee.getId())
                .schoolIdNumber(attendee.getSchoolIdNumber())
                .firstName(attendee.getFirstName())
                .middleInitial(attendee.getMiddleInitial())
                .lastName(attendee.getLastName())
                .course(attendee.getCourse())
                .yearLevel(attendee.getYearLevel())
                .build();
    }
}
