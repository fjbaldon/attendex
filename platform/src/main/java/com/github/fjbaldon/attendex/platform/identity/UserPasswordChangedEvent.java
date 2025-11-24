package com.github.fjbaldon.attendex.platform.identity;

public record UserPasswordChangedEvent(
        String email,
        String ipAddress
) {
}
