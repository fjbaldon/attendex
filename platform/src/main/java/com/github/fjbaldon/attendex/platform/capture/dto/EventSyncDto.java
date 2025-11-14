package com.github.fjbaldon.attendex.platform.capture.dto;

import java.time.Instant;
import java.util.List;

public record EventSyncDto(
        Long id,
        String name,
        List<SessionSyncDto> sessions,
        List<RosterSyncDto> roster
) {
    public record SessionSyncDto(
            Long id,
            String activityName,
            Instant targetTime,
            String intent
    ) {
    }

    public record RosterSyncDto(
            Long attendeeId,
            String identity,
            String firstName,
            String lastName,
            String qrCodeHash
    ) {
    }
}
