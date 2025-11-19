package com.github.fjbaldon.attendex.platform.organization.dto;

import java.time.Instant;

public record OrganizationDto(
        Long id,
        String name,
        String identityFormatRegex,
        String lifecycle,
        String subscriptionType,
        Instant subscriptionExpiresAt,
        Instant createdAt
) {
}
