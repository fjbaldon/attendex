package com.github.fjbaldon.attendex.platform.attendee;

public record AttributeDeletedEvent(
        Long organizationId,
        String attributeName
) {
}
