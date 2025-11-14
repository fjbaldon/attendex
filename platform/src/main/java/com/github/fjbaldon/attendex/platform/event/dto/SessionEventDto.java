package com.github.fjbaldon.attendex.platform.event.dto;

public record SessionEventDto(
        Long sessionId,
        Long eventId
) {
}
