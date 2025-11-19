package com.github.fjbaldon.attendex.platform.event;

import com.github.fjbaldon.attendex.platform.attendee.dto.AttendeeDto;
import com.github.fjbaldon.attendex.platform.capture.dto.EntryDetailsDto;
import com.github.fjbaldon.attendex.platform.event.dto.CreateEventRequestDto;
import com.github.fjbaldon.attendex.platform.event.dto.EventDto;
import com.github.fjbaldon.attendex.platform.identity.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANIZER')")
class EventController {

    private final EventFacade eventFacade;

    @PostMapping
    public ResponseEntity<EventDto> createEvent(
            @Valid @RequestBody CreateEventRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user) {

        EventDto event = eventFacade.createEvent(user.getOrganizationId(), user.getId(), request);

        return new ResponseEntity<>(event, HttpStatus.CREATED);
    }

    @GetMapping
    public Page<EventDto> getEvents(
            @RequestParam(required = false) String query,
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails user) {
        return eventFacade.findEvents(user.getOrganizationId(), query, pageable);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventDto> getEventById(
            @PathVariable Long eventId,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(eventFacade.findEventById(eventId, user.getOrganizationId()));
    }

    @PostMapping("/{eventId}/roster/{attendeeId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void addAttendeeToRoster(
            @PathVariable Long eventId,
            @PathVariable Long attendeeId,
            @AuthenticationPrincipal CustomUserDetails user) {
        eventFacade.addAttendeeToRoster(eventId, attendeeId, user.getOrganizationId());
    }

    @DeleteMapping("/{eventId}/roster/{attendeeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeAttendeeFromRoster(
            @PathVariable Long eventId,
            @PathVariable Long attendeeId) {
        eventFacade.removeAttendeeFromRoster(eventId, attendeeId);
    }

    @GetMapping("/{eventId}/roster")
    public Page<AttendeeDto> getRoster(
            @PathVariable Long eventId,
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails user) {
        return eventFacade.findRosterForEvent(eventId, user.getOrganizationId(), pageable);
    }

    @GetMapping("/{eventId}/arrivals")
    public Page<EntryDetailsDto> getArrivals(
            @PathVariable Long eventId,
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails user) {
        return eventFacade.findEntriesByIntent(eventId, user.getOrganizationId(), "Arrival", pageable);
    }

    @GetMapping("/{eventId}/departures")
    public Page<EntryDetailsDto> getDepartures(
            @PathVariable Long eventId,
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails user) {
        return eventFacade.findEntriesByIntent(eventId, user.getOrganizationId(), "Departure", pageable);
    }
}
