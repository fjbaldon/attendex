package com.github.fjbaldon.attendex.platform.admin.events;

public record StewardCreatedEvent(
        String actorEmail,
        String targetStewardEmail
) {
}
