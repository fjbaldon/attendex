package com.github.fjbaldon.attendex.platform.event;

public record SessionEventDto(
        Long sessionId,
        Long eventId
) {
}
