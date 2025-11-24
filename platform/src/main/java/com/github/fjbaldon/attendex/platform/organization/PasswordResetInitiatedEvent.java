package com.github.fjbaldon.attendex.platform.organization;

public record PasswordResetInitiatedEvent(
        String email,
        String temporaryPassword,
        String organizationName
) {
}
