package com.github.fjbaldon.attendex.platform.event.dto;

import java.time.Instant;

public record SessionDto(
        String activityName,
        Instant targetTime,
        String intent
) {
}
