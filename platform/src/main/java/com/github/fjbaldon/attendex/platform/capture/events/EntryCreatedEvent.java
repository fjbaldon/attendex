package com.github.fjbaldon.attendex.platform.capture.events;

import java.time.Instant;

public record EntryCreatedEvent(
        Long entryId,
        Long eventId,
        Long organizationId,
        Instant scanTimestamp
) {
}
