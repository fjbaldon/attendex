package com.github.fjbaldon.attendex.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ScannerRequest {

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, message = "Username must be at least 3 characters long")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;
}
