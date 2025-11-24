package com.github.fjbaldon.attendex.platform.dashboard;

import com.github.fjbaldon.attendex.platform.capture.CaptureFacade;
import com.github.fjbaldon.attendex.platform.capture.DailyEntryCount;
import com.github.fjbaldon.attendex.platform.capture.DashboardDto;
import com.github.fjbaldon.attendex.platform.common.security.CustomUserDetails;
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
class DashboardController {

    private final DashboardFacade dashboardFacade;
    private final CaptureFacade captureFacade;

    @GetMapping
    public ResponseEntity<DashboardDto> getDashboard(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(dashboardFacade.getOrganizerDashboard(user.getOrganizationId()));
    }

    @GetMapping("/activity")
    public ResponseEntity<List<DailyEntryCount>> getActivity(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "90d") String range) {

        long days = switch (range) {
            case "30d" -> 30;
            case "7d" -> 7;
            default -> 90;
        };

        Instant startDate = Instant.now().minus(days, ChronoUnit.DAYS);
        return ResponseEntity.ok(captureFacade.getDailyActivity(user.getOrganizationId(), startDate));
    }
}
