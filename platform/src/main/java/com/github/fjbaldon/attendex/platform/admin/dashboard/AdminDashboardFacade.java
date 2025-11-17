package com.github.fjbaldon.attendex.platform.admin.dashboard;

import com.github.fjbaldon.attendex.platform.admin.dashboard.dto.AdminDashboardDto;
import com.github.fjbaldon.attendex.platform.analytics.AnalyticsFacade;
import com.github.fjbaldon.attendex.platform.organization.OrganizationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardFacade {

    private final AnalyticsFacade analyticsFacade;
    private final OrganizationFacade organizationFacade;

    @Transactional(readOnly = true)
    public AdminDashboardDto getAdminDashboard() {
        var stats = new AdminDashboardDto.AdminDashboardStatsDto(
                analyticsFacade.countTotalOrganizations(),
                analyticsFacade.countOrganizationsByLifecycle("ACTIVE"),
                analyticsFacade.countOrganizationsBySubscriptionType("TRIAL"),
                analyticsFacade.countOrganizationsByLifecycle("SUSPENDED")
        );

        var expiring = organizationFacade.findExpiringSubscriptions(Instant.now().plus(30, ChronoUnit.DAYS), PageRequest.of(0, 5))
                .stream()
                .map(org -> new AdminDashboardDto.OrganizationSummaryDto(org.id(), org.name(), org.subscriptionExpiresAt()))
                .collect(Collectors.toList());

        var recent = organizationFacade.findRecentRegistrations(PageRequest.of(0, 5))
                .stream()
                .map(org -> new AdminDashboardDto.OrganizationSummaryDto(org.id(), org.name(), null)) // Date not needed here
                .collect(Collectors.toList());

        var attention = organizationFacade.findOrganizationsByLifecycle(List.of("INACTIVE", "SUSPENDED"), PageRequest.of(0, 5))
                .stream()
                .map(org -> new AdminDashboardDto.OrganizationSummaryDto(org.id(), org.name(), null))
                .collect(Collectors.toList());

        return new AdminDashboardDto(stats, expiring, recent, attention);
    }
}
