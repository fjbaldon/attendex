package com.github.fjbaldon.attendex.platform.admin.dashboard;

import com.github.fjbaldon.attendex.platform.admin.dashboard.dto.AdminDashboardDto;
import com.github.fjbaldon.attendex.platform.organization.dto.DailyRegistration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STEWARD')")
class AdminDashboardController {

    private final AdminDashboardFacade adminDashboardFacade;

    @GetMapping
    public ResponseEntity<AdminDashboardDto> getDashboard() {
        return ResponseEntity.ok(adminDashboardFacade.getAdminDashboard());
    }

    @GetMapping("/registrations")
    public ResponseEntity<List<DailyRegistration>> getRegistrationActivity(
            @RequestParam(defaultValue = "90d") String range) {
        return ResponseEntity.ok(adminDashboardFacade.getRegistrationActivity(range));
    }
}
