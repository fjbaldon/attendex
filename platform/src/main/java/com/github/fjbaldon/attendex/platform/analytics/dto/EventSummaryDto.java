package com.github.fjbaldon.attendex.platform.analytics.dto;

public record EventSummaryDto(
        Long eventId,
        Long organizationId,
        long rosterCount,
        long entryCount,
        String eventName,
        double attendanceRate
) {
}
