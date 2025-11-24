package com.github.fjbaldon.attendex.platform.capture;

import com.github.fjbaldon.attendex.platform.common.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANIZER')")
class CaptureEventQueryController {

    private final CaptureFacade captureFacade;

    @GetMapping("/{eventId}/arrivals")
    public Page<EntryDetailsDto> getArrivals(
            @PathVariable Long eventId,
            @RequestParam(required = false) String query,
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails user) {

        return captureFacade.findFilteredEntriesPaginated(eventId, user.getOrganizationId(), "Arrival", query, pageable);
    }

    @GetMapping("/{eventId}/departures")
    public Page<EntryDetailsDto> getDepartures(
            @PathVariable Long eventId,
            @RequestParam(required = false) String query,
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails user) {

        return captureFacade.findFilteredEntriesPaginated(eventId, user.getOrganizationId(), "Departure", query, pageable);
    }
}
