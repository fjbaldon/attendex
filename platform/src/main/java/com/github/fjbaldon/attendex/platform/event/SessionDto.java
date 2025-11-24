package com.github.fjbaldon.attendex.platform.event;

import java.time.Instant;

public record SessionDto(
        Long id,
        String activityName,
        Instant targetTime,
        String intent
) {
}
