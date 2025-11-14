package com.github.fjbaldon.attendex.platform.capture.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.time.Instant;
import java.util.List;

public record EntrySyncRequestDto(
        @NotEmpty List<@Valid EntryRecord> records
) {
    public record EntryRecord(
            Long sessionId,
            Long attendeeId,
            Instant scanTimestamp
    ) {
    }
}
