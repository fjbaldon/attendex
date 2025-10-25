package com.github.fjbaldon.attendex.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class EventRequest {

    @NotBlank(message = "Event name cannot be blank")
    private String eventName;

    @NotNull(message = "Start date is required")
    private Instant startDate;

    @NotNull(message = "End date is required")
    private Instant endDate;

    @NotEmpty(message = "At least one time slot is required")
    private List<@Valid TimeSlotRequest> timeSlots;

    @AssertTrue(message = "End date must be after or on the same day as the start date")
    private boolean isEndDateAfterStartDate() {
        return startDate == null || endDate == null || !endDate.isBefore(startDate);
    }
}
