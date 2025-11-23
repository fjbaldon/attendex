package com.github.fjbaldon.attendex.platform.identity.events;

public record UserLoggedInEvent(
        String email,
        String ipAddress
) {
}
