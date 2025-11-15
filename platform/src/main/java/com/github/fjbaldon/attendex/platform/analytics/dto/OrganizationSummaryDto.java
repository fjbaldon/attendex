package com.github.fjbaldon.attendex.platform.analytics.dto;

public record OrganizationSummaryDto(
        long totalEvents,
        long totalAttendees,
        long totalScanners
) {
}
