package com.github.fjbaldon.attendex.platform.attendee.dto;

import java.util.List;

public record AttributeDto(
        Long id,
        String name,
        String type,
        List<String> options
) {
}
