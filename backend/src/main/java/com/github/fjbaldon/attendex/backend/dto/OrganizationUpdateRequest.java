package com.github.fjbaldon.attendex.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OrganizationUpdateRequest {
    @NotBlank(message = "Organization name cannot be blank")
    @Size(max = 100)
    private String name;

    @Size(max = 255)
    private String identifierFormatRegex;
}
