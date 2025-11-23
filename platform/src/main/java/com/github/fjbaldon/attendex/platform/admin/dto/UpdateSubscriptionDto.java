package com.github.fjbaldon.attendex.platform.admin.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record UpdateSubscriptionDto(
        @NotBlank String subscriptionType,
        Instant expiresAt
) {
}
