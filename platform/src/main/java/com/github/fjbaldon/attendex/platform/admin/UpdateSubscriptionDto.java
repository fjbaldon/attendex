package com.github.fjbaldon.attendex.platform.admin;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record UpdateSubscriptionDto(
        @NotBlank String subscriptionType,
        Instant expiresAt
) {
}
