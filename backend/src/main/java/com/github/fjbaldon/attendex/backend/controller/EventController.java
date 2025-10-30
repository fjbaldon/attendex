package com.github.fjbaldon.attendex.backend.controller;

import com.github.fjbaldon.attendex.backend.dto.AttendeeResponse;
import com.github.fjbaldon.attendex.backend.dto.CheckedInAttendeeResponse;
import com.github.fjbaldon.attendex.backend.dto.EventRequest;
import com.github.fjbaldon.attendex.backend.dto.EventResponse;
import com.github.fjbaldon.attendex.backend.security.CustomUserDetails;
import com.github.fjbaldon.attendex.backend.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANIZER')")
public class EventController {
    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest request, @AuthenticationPrincipal CustomUserDetails user) {
        EventResponse createdEvent = eventService.createEvent(request, user.getUsername(), user.getOrganizationId());
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getOrganizationEvents(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(eventService.getAllEventsByOrganization(user.getOrganizationId()));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long eventId, @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(eventService.getEventById(eventId, user.getOrganizationId()));
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody EventRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {
        EventResponse updatedEvent = eventService.updateEvent(eventId, request, user.getOrganizationId());
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId, @AuthenticationPrincipal CustomUserDetails user) {
        eventService.deleteEvent(eventId, user.getOrganizationId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{eventId}/attendees")
    public ResponseEntity<List<AttendeeResponse>> getEventAttendees(@PathVariable Long eventId, @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(eventService.getAttendeesForEvent(eventId, user.getOrganizationId()));
    }

    @GetMapping("/{eventId}/checked-in")
    public ResponseEntity<List<CheckedInAttendeeResponse>> getCheckedInAttendees(@PathVariable Long eventId, @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(eventService.getCheckedInAttendees(eventId, user.getOrganizationId()));
    }

    @GetMapping("/{eventId}/checked-out")
    public ResponseEntity<List<CheckedInAttendeeResponse>> getCheckedOutAttendees(@PathVariable Long eventId, @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(eventService.getCheckedOutAttendees(eventId, user.getOrganizationId()));
    }

    @PostMapping("/{eventId}/attendees/{attendeeId}")
    public ResponseEntity<Void> addAttendeeToEvent(@PathVariable Long eventId, @PathVariable Long attendeeId, @AuthenticationPrincipal CustomUserDetails user) {
        eventService.addAttendeeToEvent(eventId, attendeeId, user.getOrganizationId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{eventId}/attendees/{attendeeId}")
    public ResponseEntity<Void> removeAttendeeFromEvent(@PathVariable Long eventId, @PathVariable Long attendeeId, @AuthenticationPrincipal CustomUserDetails user) {
        eventService.removeAttendeeFromEvent(eventId, attendeeId, user.getOrganizationId());
        return ResponseEntity.noContent().build();
    }
}
