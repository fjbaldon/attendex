package com.github.fjbaldon.attendex.platform.analytics;

public record EventSummaryDto(
        Long eventId,
        Long organizationId,
        long rosterCount,
        long entryCount,
        String eventName,
        double attendanceRate
) {
}
