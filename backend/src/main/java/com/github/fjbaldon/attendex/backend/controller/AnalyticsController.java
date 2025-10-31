package com.github.fjbaldon.attendex.backend.controller;

import com.github.fjbaldon.attendex.backend.dto.AnalyticsBreakdownDto;
import com.github.fjbaldon.attendex.backend.security.CustomUserDetails;
import com.github.fjbaldon.attendex.backend.service.AnalyticsService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANIZER')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/custom-fields")
    public ResponseEntity<List<String>> getAvailableCustomFields(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(analyticsService.getAvailableCustomFields(user.getOrganizationId()));
    }

    @GetMapping("/events/{eventId}/breakdown")
    public ResponseEntity<AnalyticsBreakdownDto> getCustomFieldBreakdown(
            @PathVariable Long eventId,
            @RequestParam @NotBlank String groupBy,
            @AuthenticationPrincipal CustomUserDetails user) {
        AnalyticsBreakdownDto breakdown = analyticsService.getCustomFieldBreakdown(eventId, groupBy, user.getOrganizationId());
        return ResponseEntity.ok(breakdown);
    }
}
