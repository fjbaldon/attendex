package com.github.fjbaldon.attendex.platform.event;

import java.time.Instant;
import java.util.List;

public record EventDto(
        Long id,
        String name,
        Instant startDate,
        Instant endDate,
        int graceMinutesBefore,
        int graceMinutesAfter,
        Instant createdAt,
        String status,
        List<SessionDto> sessions
) {
}
