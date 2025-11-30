package com.github.fjbaldon.attendex.platform.capture;

import com.github.fjbaldon.attendex.platform.attendee.AttendeeDto;

import java.time.Instant;

public record EntryDetailsDto(
        Long entryId,
        Long sessionId, // NEW
        Instant scanTimestamp,
        String punctuality,
        AttendeeDto attendee
) {
}
