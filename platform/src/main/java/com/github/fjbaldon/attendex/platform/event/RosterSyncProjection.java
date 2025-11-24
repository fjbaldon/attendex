package com.github.fjbaldon.attendex.platform.event;

public record RosterSyncProjection(
        Long attendeeId,
        String identity,
        String firstName,
        String lastName
) {
}
