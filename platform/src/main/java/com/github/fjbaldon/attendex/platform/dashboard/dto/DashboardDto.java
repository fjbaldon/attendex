package com.github.fjbaldon.attendex.platform.dashboard.dto;

import java.time.Instant;
import java.util.List;

public record DashboardDto(
        DashboardStatsDto stats,
        List<UpcomingEventDto> upcomingEvents,
        List<RecentEventStatsDto> recentEventStats
) {
    public record DashboardStatsDto(
            long totalEvents,
            long totalAttendees,
            long totalScanners,
            long liveEntries
    ) {
    }

    public record UpcomingEventDto(
            Long id,
            String eventName,
            Instant startDate
    ) {
    }

    public record RecentEventStatsDto(
            Long id,
            String eventName,
            long totalRoster,
            long totalEntries
    ) {
    }
}
