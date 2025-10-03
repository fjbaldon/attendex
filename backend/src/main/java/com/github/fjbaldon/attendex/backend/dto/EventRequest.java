package com.github.fjbaldon.attendex.backend.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class EventRequest {

    @NotBlank(message = "Event name cannot be blank")
    private String eventName;

    @NotNull(message = "Start date and time is required")
    private Instant startDate;

    @NotNull(message = "End date and time is required")
    private Instant endDate;

    @AssertTrue(message = "End date must be after start date")
    private boolean isEndDateAfterStartDate() {
        return startDate == null || endDate == null || endDate.isAfter(startDate);
    }
}
