package com.github.fjbaldon.attendex.platform.capture;

import com.github.fjbaldon.attendex.platform.event.EventFacade;
import com.github.fjbaldon.attendex.platform.common.security.CustomUserDetails;
import com.github.fjbaldon.attendex.platform.event.EventSyncDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/capture")
@RequiredArgsConstructor
class CaptureController {

    private final CaptureFacade captureFacade;
    private final EventFacade eventFacade;

    @GetMapping("/events")
    @PreAuthorize("hasRole('SCANNER')")
    public ResponseEntity<List<EventSyncDto>> getEventsForSync(@AuthenticationPrincipal CustomUserDetails user) {
        List<EventSyncDto> events = eventFacade.getEventsForSync(user.getOrganizationId());
        return ResponseEntity.ok(events);
    }

    @GetMapping("/events/{eventId}/attendees")
    @PreAuthorize("hasRole('SCANNER')")
    public ResponseEntity<Page<EventSyncDto.RosterSyncDto>> getAttendeesForEvent(
            @PathVariable Long eventId,
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Page<EventSyncDto.RosterSyncDto> attendees = eventFacade.getFormattedRosterForSync(eventId, user.getOrganizationId(), pageable);
        return ResponseEntity.ok(attendees);
    }

    @GetMapping("/events/{eventId}/stats")
    @PreAuthorize("hasRole('SCANNER') or hasRole('ORGANIZER')")
    public ResponseEntity<EventStatsDto> getEventStats(
            @PathVariable Long eventId,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(captureFacade.getEventStats(user.getOrganizationId(), eventId));
    }

    @PostMapping("/sync")
    @PreAuthorize("hasRole('SCANNER')")
    public ResponseEntity<BatchSyncResponse> syncEntries(
            @Valid @RequestBody EntrySyncRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user) {

        BatchSyncResponse response = captureFacade.syncEntries(
                user.getOrganizationId(),
                user.getUsername(),
                request
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/orphans")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Page<OrphanedEntryDto>> getOrphanedEntries(
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(captureFacade.getOrphanedEntries(user.getOrganizationId(), pageable));
    }

    @PostMapping("/orphans/{id}/recover")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Void> recoverOrphan(
            @PathVariable Long id,
            @RequestParam Long targetEventId,
            @AuthenticationPrincipal CustomUserDetails user) {

        captureFacade.recoverOrphan(user.getOrganizationId(), id, targetEventId, user.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/orphans/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Void> deleteOrphan(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {
        captureFacade.deleteOrphanedEntry(user.getOrganizationId(), id);
        return ResponseEntity.noContent().build();
    }
}
