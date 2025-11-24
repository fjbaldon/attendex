package com.github.fjbaldon.attendex.platform.identity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequestDto(
        @NotBlank @Size(min = 8) String newPassword
) {
}
