package com.github.fjbaldon.attendex.platform.admin;

import jakarta.validation.constraints.NotBlank;

public record UpdateOrganizationLifecycleDto(
        @NotBlank String lifecycle
) {
}
