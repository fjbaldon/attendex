package com.github.fjbaldon.attendex.platform.capture;

import java.time.Instant;

public record EntryEventStatusDto(
        Long eventId,
        Long sessionId,
        Instant scanTimestamp,
        String punctuality
) {}
