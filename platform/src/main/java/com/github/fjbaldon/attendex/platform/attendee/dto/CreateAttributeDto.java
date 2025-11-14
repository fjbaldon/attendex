package com.github.fjbaldon.attendex.platform.attendee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateAttributeDto(
        @NotBlank String name,
        @NotBlank String type,
        @NotEmpty List<String> options
) {
}
