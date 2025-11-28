package com.github.fjbaldon.attendex.platform.analytics;

public record CohortStatsDto(
        long totalCohortSize,
        long presentCount,
        long absentCount,
        double attendanceRate
) {}
