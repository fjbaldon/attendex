package com.github.fjbaldon.attendex.platform.event.dto;

public record RosterSyncProjection(
        Long attendeeId,
        String identity,
        String firstName,
        String lastName
) {
}
