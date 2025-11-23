package com.github.fjbaldon.attendex.platform.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record UpdateSessionDto(
        Long id,
        @NotBlank String activityName,
        @NotNull Instant targetTime,
        @NotBlank String intent
) {
}
