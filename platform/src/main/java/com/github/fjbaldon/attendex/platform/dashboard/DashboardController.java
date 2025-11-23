package com.github.fjbaldon.attendex.platform.dashboard;

import com.github.fjbaldon.attendex.platform.dashboard.dto.DashboardDto;
import com.github.fjbaldon.attendex.platform.identity.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANIZER')")
class DashboardController {

    private final DashboardFacade dashboardFacade;

    @GetMapping
    public ResponseEntity<DashboardDto> getDashboard(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(dashboardFacade.getOrganizerDashboard(user.getOrganizationId()));
    }
}
