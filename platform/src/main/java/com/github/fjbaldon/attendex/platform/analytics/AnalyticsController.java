package com.github.fjbaldon.attendex.platform.analytics;

import com.github.fjbaldon.attendex.platform.analytics.dto.AttributeBreakdownDto;
import com.github.fjbaldon.attendex.platform.identity.CustomUserDetails;
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

    @GetMapping("/events/{eventId}/breakdown")
    public ResponseEntity<AttributeBreakdownDto> getAttributeBreakdown(
            @PathVariable Long eventId,
            @RequestParam String attributeName,
            @AuthenticationPrincipal CustomUserDetails user) {

        // In a real app, we would first verify the user has access to this eventId
        return ResponseEntity.ok(analyticsFacade.getAttributeBreakdown(eventId, attributeName));
    }
}
