package com.github.fjbaldon.attendex.platform.analytics;

public record OrganizationSummaryDto(
        long totalEvents,
        long totalAttendees,
        long totalScanners
) {
}
