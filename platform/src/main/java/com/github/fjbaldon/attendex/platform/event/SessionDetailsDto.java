package com.github.fjbaldon.attendex.platform.event;

import java.time.Instant;

public record SessionDetailsDto(
        Long sessionId,
        Instant targetTime,
        int graceMinutesBefore,
        int graceMinutesAfter
) {
}
