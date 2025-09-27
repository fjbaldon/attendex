package com.github.fjbaldon.attendex.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AttendeeResponse {
    private Long id;
    private String uniqueIdentifier;
    private String firstName;
    private String lastName;
    private Map<String, Object> customFields;
}
