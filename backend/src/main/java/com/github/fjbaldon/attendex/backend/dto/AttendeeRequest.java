package com.github.fjbaldon.attendex.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AttendeeRequest {
    @NotBlank(message = "School ID number is required")
    private String schoolIdNumber;

    @NotBlank(message = "First name is required")
    private String firstName;

    private Character middleInitial;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String course;
    private Integer yearLevel;
}
