package com.github.fjbaldon.attendex.platform.attendee.dto;

import java.util.List;
import java.util.Map;

public record AttendeeImportAnalysisDto(
        List<CreateAttendeeDto> attendeesToCreate, // New Records
        List<CreateAttendeeDto> attendeesToUpdate, // Existing Records (if Mode=UPDATE)
        List<InvalidRow> invalidRows,
        List<String> newAttributesToCreate // List of attribute names that will be auto-generated
) {
    public record InvalidRow(
            long rowNumber,
            Map<String, String> rowData,
            String error
    ) {
    }
}
