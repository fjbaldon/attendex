package com.github.fjbaldon.attendex.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class AttendeeRequest {
    @NotBlank(message = "Unique identifier is required")
    private String uniqueIdentifier;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private Map<String, Object> customFields;
}
