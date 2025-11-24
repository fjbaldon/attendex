package com.github.fjbaldon.attendex.platform.capture;

import java.time.Instant;
import java.util.List;

public record EventStatsDto(
        long totalScans,
        long totalRoster,
        double attendanceRate,
        Instant firstScan,
        Instant lastScan,
        List<StatItem> sessionStats,
        List<StatItem> scannerStats
) {
    public record StatItem(String label, long count) {}
}
