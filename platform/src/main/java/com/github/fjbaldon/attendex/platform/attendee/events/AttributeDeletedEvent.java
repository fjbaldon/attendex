package com.github.fjbaldon.attendex.platform.attendee.events;

public record AttributeDeletedEvent(
        Long organizationId,
        String attributeName
) {
}
