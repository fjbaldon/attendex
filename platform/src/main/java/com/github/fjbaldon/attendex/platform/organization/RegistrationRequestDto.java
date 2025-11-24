package com.github.fjbaldon.attendex.platform.organization;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrationRequestDto(
        @NotBlank String organizationName,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password
) {
}
