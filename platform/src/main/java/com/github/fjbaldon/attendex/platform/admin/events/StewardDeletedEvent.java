package com.github.fjbaldon.attendex.platform.admin.events;

public record StewardDeletedEvent(
        String actorEmail,
        String targetStewardEmail
) {
}
