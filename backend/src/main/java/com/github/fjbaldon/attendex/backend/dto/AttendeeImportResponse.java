package com.github.fjbaldon.attendex.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttendeeImportResponse {
    private String message;
    private int successfulImports;
    private int failedImports;
    private List<String> errors;
}
