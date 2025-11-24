package com.github.fjbaldon.attendex.platform.attendee;

import java.time.Instant;
import java.util.Map;

public record AttendeeDto(
        Long id,
        String identity,
        String firstName,
        String lastName,
        Map<String, Object> attributes,
        Instant createdAt
) {
}
