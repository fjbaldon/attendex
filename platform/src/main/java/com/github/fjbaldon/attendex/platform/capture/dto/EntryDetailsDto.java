package com.github.fjbaldon.attendex.platform.capture.dto;

import com.github.fjbaldon.attendex.platform.attendee.dto.AttendeeDto;

import java.time.Instant;

public record EntryDetailsDto(
        Long entryId,
        Instant scanTimestamp,
        String punctuality,
        AttendeeDto attendee
) {
}
