package com.github.fjbaldon.attendex.platform.analytics.dto;

import java.util.List;

public record AttributeBreakdownDto(
        String attributeName,
        List<BreakdownItem> breakdown
) {
    public record BreakdownItem(
            String value,
            long count
    ) {
    }
}
