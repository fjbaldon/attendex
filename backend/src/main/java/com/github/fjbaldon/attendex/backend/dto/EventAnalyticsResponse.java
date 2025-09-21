package com.github.fjbaldon.attendex.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
public class EventAnalyticsResponse {
    private Long eventId;
    private String eventName;
    private long totalRegistered;
    private long totalCheckedIn;
    private double attendanceRate;
    private Map<LocalDate, Long> checkInsByDate;
}
