package com.github.fjbaldon.attendex.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class EventResponse {
    private Long id;
    private String eventName;
    private LocalDate startDate;
    private LocalDate endDate;
}
