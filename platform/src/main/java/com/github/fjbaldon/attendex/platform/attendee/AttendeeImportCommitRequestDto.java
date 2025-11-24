package com.github.fjbaldon.attendex.platform.attendee;

import jakarta.validation.Valid;

import java.util.List;

public record AttendeeImportCommitRequestDto(
        @Valid List<CreateAttendeeDto> attendees,
        boolean updateExisting, // If true, we use save() to overwrite. If false, we expect new entities.
        List<String> newAttributes // Attributes to auto-create
) {
}
