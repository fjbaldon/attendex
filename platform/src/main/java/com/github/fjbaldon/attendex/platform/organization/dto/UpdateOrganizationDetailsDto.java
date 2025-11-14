package com.github.fjbaldon.attendex.platform.organization.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateOrganizationDetailsDto(
        @NotBlank String name,
        String identityFormatRegex
) {
}
