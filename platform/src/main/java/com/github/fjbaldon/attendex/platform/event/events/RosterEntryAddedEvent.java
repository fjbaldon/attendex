package com.github.fjbaldon.attendex.platform.event.events;

public record RosterEntryAddedEvent(
        Long eventId,
        Long organizationId,
        Long attendeeId
) {
}
