package com.github.fjbaldon.attendex.platform.event.dto;

import java.time.Instant;
import java.util.List;

public record EventForSyncDto(
        Long id,
        Long organizationId,
        String name,
        Instant startDate,
        Instant endDate,
        List<SessionForSyncDto> sessions,
        List<RosterEntryForSyncDto> rosterEntries
) {
    public record SessionForSyncDto(
            Long id,
            String activityName,
            Instant targetTime,
            String intent
    ) {
    }

    public record RosterEntryForSyncDto(
            Long attendeeId
    ) {
    }
}
