package com.github.fjbaldon.attendex.platform.attendee.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record UpdateAttendeeDto(
        @NotBlank String firstName,
        @NotBlank String lastName,
        Map<String, Object> attributes
) {
}
