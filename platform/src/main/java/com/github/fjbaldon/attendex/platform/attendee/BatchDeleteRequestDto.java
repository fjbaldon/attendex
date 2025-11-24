package com.github.fjbaldon.attendex.platform.attendee;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BatchDeleteRequestDto(
        @NotEmpty
        @Size(max = 500, message = "Cannot delete more than 500 items at once")
        List<Long> ids
) {
}
