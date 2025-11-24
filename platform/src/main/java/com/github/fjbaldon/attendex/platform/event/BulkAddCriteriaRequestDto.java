package com.github.fjbaldon.attendex.platform.event;

import java.util.Map;

public record BulkAddCriteriaRequestDto(
        String query,
        Map<String, String> attributes
) {
}
