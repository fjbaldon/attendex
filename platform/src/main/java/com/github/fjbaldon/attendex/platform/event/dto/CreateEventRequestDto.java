package com.github.fjbaldon.attendex.platform.event.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.List;

public record CreateEventRequestDto(
        @NotBlank String name,
        @NotNull Instant startDate,
        @NotNull Instant endDate,
        @Min(0) int graceMinutesBefore,
        @Min(0) int graceMinutesAfter,
        @NotEmpty List<@Valid SessionDto> sessions
) {
    @AssertTrue(message = "End date must be after or equal to start date")
    private boolean isEndDateValid() {
        return !endDate.isBefore(startDate);
    }
}
