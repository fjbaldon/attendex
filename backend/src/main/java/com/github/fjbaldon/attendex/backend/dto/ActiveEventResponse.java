package com.github.fjbaldon.attendex.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiveEventResponse {
    private Long id;
    private String eventName;
    private List<TimeSlotResponse> timeSlots;
}
