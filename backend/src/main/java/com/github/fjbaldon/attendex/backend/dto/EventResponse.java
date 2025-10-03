package com.github.fjbaldon.attendex.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant; // CHANGED

@Data
@Builder
public class EventResponse {
    private Long id;
    private String eventName;
    private Instant startDate;
    private Instant endDate;
}
