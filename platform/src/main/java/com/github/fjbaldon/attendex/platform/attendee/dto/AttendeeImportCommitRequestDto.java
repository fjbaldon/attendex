package com.github.fjbaldon.attendex.platform.attendee.dto;

import jakarta.validation.Valid;

import java.util.List;

public record AttendeeImportCommitRequestDto(
        @Valid List<CreateAttendeeDto> attendees
) {
}
