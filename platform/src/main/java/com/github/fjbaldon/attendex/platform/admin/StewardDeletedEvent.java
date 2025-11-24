package com.github.fjbaldon.attendex.platform.admin;

public record StewardDeletedEvent(
        String actorEmail,
        String targetStewardEmail
) {
}
