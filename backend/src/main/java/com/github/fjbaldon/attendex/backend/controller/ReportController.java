package com.github.fjbaldon.attendex.backend.controller;

import com.github.fjbaldon.attendex.backend.dto.EventAnalyticsResponse;
import com.github.fjbaldon.attendex.backend.security.CustomUserDetails;
import com.github.fjbaldon.attendex.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('VIEW_REPORTS')")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/events/{eventId}/analytics")
    public ResponseEntity<EventAnalyticsResponse> getEventAnalytics(
            @PathVariable Long eventId,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(reportService.getEventAnalytics(eventId, user.getOrganizationId()));
    }
}
