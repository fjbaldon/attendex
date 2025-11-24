package com.github.fjbaldon.attendex.platform.event;

public record EventCreatedEvent(
        Long eventId,
        Long organizationId,
        String eventName
) {
}
