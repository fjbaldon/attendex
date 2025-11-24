package com.github.fjbaldon.attendex.platform.attendee;

public record AttendeeCreatedEvent(
        Long attendeeId,
        Long organizationId
) {
}
