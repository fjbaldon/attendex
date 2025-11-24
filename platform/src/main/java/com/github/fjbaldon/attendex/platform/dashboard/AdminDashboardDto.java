package com.github.fjbaldon.attendex.platform.dashboard;

import java.time.Instant;
import java.util.List;

public record AdminDashboardDto(
        AdminDashboardStatsDto stats,
        List<OrganizationSummaryDto> expiringSubscriptions,
        List<OrganizationSummaryDto> recentRegistrations,
        List<OrganizationSummaryDto> attentionRequired
) {
    public record AdminDashboardStatsDto(
            long totalOrganizations,
            long activeOrganizations,
            long trialSubscriptions,
            long suspendedAccounts
    ) {
    }

    public record OrganizationSummaryDto(
            Long id,
            String name,
            Instant date // Represents either expiry or creation date
    ) {
    }
}
