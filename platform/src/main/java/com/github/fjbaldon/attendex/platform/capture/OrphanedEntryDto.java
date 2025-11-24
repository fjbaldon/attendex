package com.github.fjbaldon.attendex.platform.capture;

import java.time.Instant;
import java.util.Map;

public record OrphanedEntryDto(
        Long id,
        Long originalEventId,
        String originalEventName,
        String scanUuid,
        String failureReason,
        Instant createdAt,
        Map<String, Object> rawPayload
) {
}
