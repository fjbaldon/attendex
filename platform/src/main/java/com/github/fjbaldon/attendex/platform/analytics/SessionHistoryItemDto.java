package com.github.fjbaldon.attendex.platform.analytics;

import java.time.Instant;

public record SessionHistoryItemDto(
        Long sessionId,
        String activityName,
        String intent, // Arrival vs Departure
        Instant targetTime,
        String status, // PRESENT, LATE, ABSENT, PENDING
        Instant scanTime
) {
}
