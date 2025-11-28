package com.github.fjbaldon.attendex.platform.analytics;

import java.time.Instant;
import java.util.Map;

public record CohortAttendeeDto(
        Long id,
        String identity,
        String firstName,
        String lastName,
        String status, // "PRESENT" or "ABSENT"
        Instant scanTime,
        Map<String, Object> attributes
) {}
