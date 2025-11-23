package com.github.fjbaldon.attendex.platform.capture.dto;

import java.time.Instant;

public record OrphanedEntryDto(
        Long id,
        Long originalEventId,
        String scanUuid,
        String failureReason,
        Instant createdAt,
        String rawPayload
) {
}
