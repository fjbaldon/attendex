package com.github.fjbaldon.attendex.platform.event;

public record RosterEntryAddedEvent(
        Long eventId,
        Long organizationId,
        Long attendeeId
) {
}
