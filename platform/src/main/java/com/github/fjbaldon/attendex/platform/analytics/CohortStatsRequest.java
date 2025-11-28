package com.github.fjbaldon.attendex.platform.analytics;

import java.util.Map;

public record CohortStatsRequest(
        Long sessionId,
        Map<String, String> filters
) {}
