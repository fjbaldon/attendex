package com.github.fjbaldon.attendex.platform.analytics;

import java.time.Instant;
import java.util.List;

public record AttendeeHistoryItemDto(
        Long eventId,
        String eventName,
        Instant eventDate,
        int sessionsCompleted, // New: 3
        int totalSessions,     // New: 4
        List<SessionHistoryItemDto> sessions // New: Detailed list
) {
}
