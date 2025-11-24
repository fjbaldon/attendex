package com.github.fjbaldon.attendex.platform.organization;

public record OrganizationRegisteredEvent(
        String organizerEmail,
        String organizationName,
        String verificationToken
) {
}
