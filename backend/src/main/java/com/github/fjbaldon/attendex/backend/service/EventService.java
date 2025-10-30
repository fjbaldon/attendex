package com.github.fjbaldon.attendex.backend.service;

import com.github.fjbaldon.attendex.backend.dto.*;
import com.github.fjbaldon.attendex.backend.model.*;
import com.github.fjbaldon.attendex.backend.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final OrganizerRepository organizerRepository;
    private final AttendeeRepository attendeeRepository;
    private final EventAttendeeRepository eventAttendeeRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;

    @Transactional
    public EventResponse createEvent(EventRequest request, String organizerEmail, Long organizationId) {
        Organizer organizer = findOrganizerByEmail(organizerEmail);

        Organization orgReference = new Organization();
        orgReference.setId(organizationId);

        Event event = Event.builder()
                .eventName(request.getEventName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .organizer(organizer)
                .organization(orgReference)
                .build();

        List<EventTimeSlot> timeSlots = request.getTimeSlots().stream()
                .map(ts -> toEventTimeSlot(ts, event))
                .toList();
        event.getTimeSlots().addAll(timeSlots);

        Event savedEvent = eventRepository.save(event);
        return toEventResponse(savedEvent);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getAllEventsByOrganization(Long organizationId) {
        return eventRepository.findAllByOrganizationId(organizationId).stream()
                .map(this::toEventResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventResponse getEventById(Long eventId, Long organizationId) {
        Event event = findEventByIdAndOrgId(eventId, organizationId);
        return toEventResponse(event);
    }

    @Transactional
    public EventResponse updateEvent(Long eventId, EventRequest request, Long organizationId) {
        Event event = findEventByIdAndOrgId(eventId, organizationId);
        event.setEventName(request.getEventName());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());

        event.getTimeSlots().clear();
        List<EventTimeSlot> newTimeSlots = request.getTimeSlots().stream()
                .map(ts -> toEventTimeSlot(ts, event))
                .toList();
        event.getTimeSlots().addAll(newTimeSlots);

        Event updatedEvent = eventRepository.save(event);
        return toEventResponse(updatedEvent);
    }

    @Transactional
    public void deleteEvent(Long eventId, Long organizationId) {
        Event event = findEventByIdAndOrgId(eventId, organizationId);
        eventRepository.delete(event);
    }

    @Transactional
    public void addAttendeeToEvent(Long eventId, Long attendeeId, Long organizationId) {
        Event event = findEventByIdAndOrgId(eventId, organizationId);
        Attendee attendee = findAttendeeByIdAndOrgId(attendeeId, organizationId);

        EventAttendee eventAttendee = EventAttendee.builder()
                .event(event)
                .attendee(attendee)
                .build();
        eventAttendeeRepository.save(eventAttendee);
    }

    @Transactional
    public void removeAttendeeFromEvent(Long eventId, Long attendeeId, Long organizationId) {
        findEventByIdAndOrgId(eventId, organizationId);
        eventAttendeeRepository.deleteByEventIdAndAttendeeId(eventId, attendeeId);
    }

    @Transactional(readOnly = true)
    public List<AttendeeResponse> getAttendeesForEvent(Long eventId, Long organizationId) {
        Event event = findEventByIdAndOrgId(eventId, organizationId);
        return event.getEventAttendees().stream()
                .map(registration -> toAttendeeResponse(registration.getAttendee()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CheckedInAttendeeResponse> getCheckedInAttendees(Long eventId, Long organizationId) {
        return getAttendeesByType(eventId, organizationId, TimeSlotType.CHECK_IN);
    }

    @Transactional(readOnly = true)
    public List<CheckedInAttendeeResponse> getCheckedOutAttendees(Long eventId, Long organizationId) {
        return getAttendeesByType(eventId, organizationId, TimeSlotType.CHECK_OUT);
    }

    private CheckedInAttendeeResponse toCheckedInAttendeeResponse(Attendee attendee, Instant checkInTimestamp) {
        return CheckedInAttendeeResponse.builder()
                .id(attendee.getId())
                .uniqueIdentifier(attendee.getUniqueIdentifier())
                .firstName(attendee.getFirstName())
                .lastName(attendee.getLastName())
                .customFields(attendee.getCustomFields())
                .checkInTimestamp(checkInTimestamp)
                .build();
    }

    private Event findEventByIdAndOrgId(Long eventId, Long organizationId) {
        return eventRepository.findById(eventId)
                .filter(event -> event.getOrganization().getId().equals(organizationId))
                .orElseThrow(() -> new EntityNotFoundException("Event with ID " + eventId + " not found in your organization"));
    }

    private Attendee findAttendeeByIdAndOrgId(Long attendeeId, Long organizationId) {
        return attendeeRepository.findByIdAndOrganizationId(attendeeId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Attendee with ID " + attendeeId + " not found in your organization"));
    }

    private Organizer findOrganizerByEmail(String email) {
        return organizerRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Organizer not found"));
    }

    private List<CheckedInAttendeeResponse> getAttendeesByType(Long eventId, Long organizationId, TimeSlotType type) {
        findEventByIdAndOrgId(eventId, organizationId);
        return attendanceRecordRepository.findByEventIdAndType(eventId, type).stream()
                .map(record -> toCheckedInAttendeeResponse(record.getAttendee(), record.getCheckInTimestamp()))
                .collect(Collectors.toList());
    }

    private EventResponse toEventResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .eventName(event.getEventName())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .timeSlots(event.getTimeSlots().stream().map(this::toTimeSlotResponse).collect(Collectors.toList()))
                .build();
    }

    private AttendeeResponse toAttendeeResponse(Attendee attendee) {
        return AttendeeResponse.builder()
                .id(attendee.getId())
                .uniqueIdentifier(attendee.getUniqueIdentifier())
                .firstName(attendee.getFirstName())
                .lastName(attendee.getLastName())
                .customFields(attendee.getCustomFields())
                .build();
    }

    private EventTimeSlot toEventTimeSlot(TimeSlotRequest request, Event event) {
        return EventTimeSlot.builder()
                .event(event)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .type(request.getType())
                .build();
    }

    private TimeSlotResponse toTimeSlotResponse(EventTimeSlot timeSlot) {
        return TimeSlotResponse.builder()
                .startTime(timeSlot.getStartTime())
                .endTime(timeSlot.getEndTime())
                .type(timeSlot.getType())
                .build();
    }
}
