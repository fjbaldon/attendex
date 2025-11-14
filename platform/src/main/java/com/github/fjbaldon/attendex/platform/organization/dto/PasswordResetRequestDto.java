package com.github.fjbaldon.attendex.platform.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetRequestDto(
        @NotBlank @Size(min = 8) String newPassword
) {
}
