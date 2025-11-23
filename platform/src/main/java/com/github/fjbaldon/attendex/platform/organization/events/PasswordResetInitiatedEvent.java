package com.github.fjbaldon.attendex.platform.organization.events;

public record PasswordResetInitiatedEvent(
        String email,
        String temporaryPassword,
        String organizationName
) {
}
