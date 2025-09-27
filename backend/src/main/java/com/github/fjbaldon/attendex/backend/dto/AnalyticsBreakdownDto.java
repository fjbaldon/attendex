package com.github.fjbaldon.attendex.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

    @Data
@AllArgsConstructor
public class AnalyticsBreakdownDto {
    private String groupName;
    private Long count;
}
