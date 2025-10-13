package com.github.fjbaldon.attendex.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsDto {
    private long totalEvents;
    private long totalAttendees;
    private long totalScanners;
    private long liveCheckIns;
}
