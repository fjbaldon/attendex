package com.github.fjbaldon.attendex.platform.organization;

import jakarta.validation.constraints.NotBlank;

public record UpdateOrganizationDetailsDto(
        @NotBlank String name,
        String identityFormatRegex
) {
}
