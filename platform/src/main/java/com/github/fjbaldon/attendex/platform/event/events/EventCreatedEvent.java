package com.github.fjbaldon.attendex.platform.event.events;

public record EventCreatedEvent(
        Long eventId,
        Long organizationId,
        String eventName
) {
}
