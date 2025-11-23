package com.github.fjbaldon.attendex.platform.capture.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public record EntrySyncRequestDto(
        @NotEmpty List<@Valid EntryRecord> records
) {
    public record EntryRecord(
            @NotNull String scanUuid,
            Long eventId,
            Long attendeeId,
            Instant scanTimestamp,

            String snapshotIdentity,
            String snapshotFirstName,
            String snapshotLastName
    ) {
    }
}
