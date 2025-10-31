package com.github.fjbaldon.attendex.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendeeImportAnalysisDto {
    private List<AttendeeRequest> validAttendees;
    private List<InvalidRowDto> invalidRows;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvalidRowDto {
        private long rowNumber;
        private Map<String, String> rowData;
        private String error;
    }
}
