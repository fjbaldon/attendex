package com.github.fjbaldon.attendex.platform.identity.events;

public record UserPasswordChangedEvent(
        String email,
        String ipAddress
) {
}
