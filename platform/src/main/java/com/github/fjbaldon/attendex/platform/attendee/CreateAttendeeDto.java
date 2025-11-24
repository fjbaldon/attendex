package com.github.fjbaldon.attendex.platform.attendee;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record CreateAttendeeDto(
        @NotBlank String identity,
        @NotBlank String firstName,
        @NotBlank String lastName,
        Map<String, Object> attributes
) {
}
