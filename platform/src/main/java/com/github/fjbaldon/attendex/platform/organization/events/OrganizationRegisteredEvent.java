package com.github.fjbaldon.attendex.platform.organization.events;

public record OrganizationRegisteredEvent(
        String organizerEmail,
        String organizationName,
        String verificationToken
) {
}
