package com.github.fjbaldon.attendex.platform.attendee.events;

public record AttendeeCreatedEvent(
        Long attendeeId,
        Long organizationId
) {
}
