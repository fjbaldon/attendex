package com.github.fjbaldon.attendex.backend.dto;

import jakarta.validation.Valid;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class AttendeeImportCommitRequest {
    private List<@Valid AttendeeRequest> attendees;
}
