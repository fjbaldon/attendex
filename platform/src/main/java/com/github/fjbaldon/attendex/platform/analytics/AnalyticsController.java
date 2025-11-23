package com.github.fjbaldon.attendex.platform.analytics;

import com.github.fjbaldon.attendex.platform.analytics.dto.AttributeBreakdownDto;
import com.github.fjbaldon.attendex.platform.analytics.dto.EventStatsDto;
import com.github.fjbaldon.attendex.platform.analytics.dto.EventSummaryDto;
import com.github.fjbaldon.attendex.platform.identity.CustomUserDetails;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANIZER')")
class AnalyticsController {

    private final AnalyticsFacade analyticsFacade;

    @GetMapping("/events/{eventId}/summary")
    public ResponseEntity<EventSummaryDto> getEventSummary(
            @PathVariable Long eventId,
            @AuthenticationPrincipal CustomUserDetails user) {

        EventSummaryDto summary = analyticsFacade.findEventSummary(eventId)
                .filter(s -> s.organizationId().equals(user.getOrganizationId()))
                .orElseThrow(() -> new EntityNotFoundException("Event summary not found or you do not have permission to view it."));

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/events/{eventId}/breakdown")
    public ResponseEntity<AttributeBreakdownDto> getAttributeBreakdown(
            @PathVariable Long eventId,
            @RequestParam String attributeName,
            @AuthenticationPrincipal CustomUserDetails user) {

        analyticsFacade.findEventSummary(eventId)
                .filter(s -> s.organizationId().equals(user.getOrganizationId()))
                .orElseThrow(() -> new EntityNotFoundException("Event not found or you do not have permission to view it."));

        return ResponseEntity.ok(analyticsFacade.getAttributeBreakdown(eventId, attributeName));
    }

    @GetMapping("/events/{eventId}/stats")
    public ResponseEntity<EventStatsDto> getEventStats(
            @PathVariable Long eventId,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(analyticsFacade.getEventStats(user.getOrganizationId(), eventId));
    }
}
