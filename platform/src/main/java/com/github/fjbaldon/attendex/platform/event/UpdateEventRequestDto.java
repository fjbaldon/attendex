package com.github.fjbaldon.attendex.platform.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.List;

public record UpdateEventRequestDto(
        @NotBlank String name,
        @NotNull Instant startDate,
        @NotNull Instant endDate,
        @Min(0) int graceMinutesBefore,
        @Min(0) int graceMinutesAfter,
        @NotEmpty List<@Valid UpdateSessionDto> sessions
) {
    @AssertTrue(message = "End date must be after or equal to start date")
    private boolean isEndDateValid() {
        return !endDate.isBefore(startDate);
    }
}
