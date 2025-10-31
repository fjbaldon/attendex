package com.github.fjbaldon.attendex.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class DashboardDto {
    private DashboardStatsDto stats;
    private List<UpcomingEventDto> upcomingEvents;
    private List<RecentEventStatsDto> recentEventStats;

    @Data
    @Builder
    public static class UpcomingEventDto {
        private Long id;
        private String eventName;
        private Instant startDate;
    }

    @Data
    @Builder
    public static class RecentEventStatsDto {
        private Long id;
        private String eventName;
        private long totalRegistered;
        private long totalCheckedIn;
    }
}
