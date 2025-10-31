package com.github.fjbaldon.attendex.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsBreakdownDto {
    private List<BreakdownItem> breakdown;
    private long totalCheckedIn;

    @Data
    @AllArgsConstructor
    public static class BreakdownItem {
        private String groupName;
        private Long count;
    }
}
