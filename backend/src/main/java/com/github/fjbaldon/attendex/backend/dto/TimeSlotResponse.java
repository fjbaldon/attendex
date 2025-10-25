package com.github.fjbaldon.attendex.backend.dto;

import com.github.fjbaldon.attendex.backend.model.TimeSlotType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class TimeSlotResponse {
    private Long id;
    private Instant startTime;
    private Instant endTime;
    private TimeSlotType type;
}
