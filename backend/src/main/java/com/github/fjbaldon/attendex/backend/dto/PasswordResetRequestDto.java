package com.github.fjbaldon.attendex.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordResetRequestDto {
    @NotBlank(message = "New temporary password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String newTemporaryPassword;
}
