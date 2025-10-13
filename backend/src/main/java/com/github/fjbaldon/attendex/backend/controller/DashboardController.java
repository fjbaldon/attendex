package com.github.fjbaldon.attendex.backend.controller;

import com.github.fjbaldon.attendex.backend.dto.DailyActivityDto;
import com.github.fjbaldon.attendex.backend.dto.DashboardStatsDto;
import com.github.fjbaldon.attendex.backend.security.CustomUserDetails;
import com.github.fjbaldon.attendex.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANIZER')")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDto> getStats(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(dashboardService.getOrganizationStats(user.getOrganizationId()));
    }

    @GetMapping("/activity")
    public ResponseEntity<List<DailyActivityDto>> getActivity(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "90d") String range) {

        long days = switch (range) {
            case "30d" -> 30;
            case "7d" -> 7;
            default -> 90;
        };
        Instant startDate = Instant.now().minus(days, ChronoUnit.DAYS);
        return ResponseEntity.ok(dashboardService.getActivityOverTime(user.getOrganizationId(), startDate));
    }
}
