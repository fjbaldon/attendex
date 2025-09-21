package com.github.fjbaldon.attendex.backend.controller;

import com.github.fjbaldon.attendex.backend.dto.AttendeeResponse;
import com.github.fjbaldon.attendex.backend.dto.EventRequest;
import com.github.fjbaldon.attendex.backend.dto.EventResponse;
import com.github.fjbaldon.attendex.backend.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANIZER')")
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest request, Principal principal) {
        EventResponse createdEvent = eventService.createEvent(request, principal.getName());
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getOrganizerEvents(Principal principal) {
        return ResponseEntity.ok(eventService.getAllEventsByOrganizer(principal.getName()));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long eventId, Principal principal) {
        return ResponseEntity.ok(eventService.getEventById(eventId, principal.getName()));
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody EventRequest request,
            Principal principal) {
        EventResponse updatedEvent = eventService.updateEvent(eventId, request, principal.getName());
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId, Principal principal) {
        eventService.deleteEvent(eventId, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{eventId}/attendees")
    public ResponseEntity<List<AttendeeResponse>> getEventAttendees(@PathVariable Long eventId, Principal principal) {
        return ResponseEntity.ok(eventService.getAttendeesForEvent(eventId, principal.getName()));
    }

    @PostMapping("/{eventId}/attendees/{attendeeId}")
    public ResponseEntity<Void> addAttendeeToEvent(@PathVariable Long eventId, @PathVariable Long attendeeId, Principal principal) {
        eventService.addAttendeeToEvent(eventId, attendeeId, principal.getName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{eventId}/attendees/{attendeeId}")
    public ResponseEntity<Void> removeAttendeeFromEvent(@PathVariable Long eventId, @PathVariable Long attendeeId, Principal principal) {
        eventService.removeAttendeeFromEvent(eventId, attendeeId, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
