package com.github.fjbaldon.attendex.platform.capture;

import java.time.Instant;

public record EntryCreatedEvent(
        Long entryId,
        Long eventId,
        Long organizationId,
        Long sessionId, // Added
        Long scannerId, // Added
        Instant scanTimestamp
) {
}
