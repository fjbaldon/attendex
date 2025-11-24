package com.github.fjbaldon.attendex.platform.identity;

public record UserLoggedInEvent(
        String email,
        String ipAddress
) {
}
