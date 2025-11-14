package com.github.fjbaldon.attendex.platform.attendee.dto;

import java.util.List;
import java.util.Map;

public record AttendeeImportAnalysisDto(
        List<CreateAttendeeDto> validAttendees,
        List<InvalidRow> invalidRows
) {
    public record InvalidRow(
            long rowNumber,
            Map<String, String> rowData,
            String error
    ) {
    }
}
