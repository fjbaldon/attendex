package com.github.fjbaldon.attendex.platform.admin;

public record StewardCreatedEvent(
        String actorEmail,
        String targetStewardEmail
) {
}
