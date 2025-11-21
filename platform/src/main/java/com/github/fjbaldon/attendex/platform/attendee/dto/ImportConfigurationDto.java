package com.github.fjbaldon.attendex.platform.attendee.dto;

import java.util.Map;

public record ImportConfigurationDto(
        // How to handle duplicates?
        ImportMode mode,
        // Should we create attributes for unknown columns?
        boolean createMissingAttributes,
        // Map CSV Header -> System Field (e.g., "Student ID" -> "identity")
        Map<String, String> columnMapping
) {
    public enum ImportMode {
        SKIP,   // Ignore duplicates (Default)
        UPDATE, // Overwrite name/attributes
        FAIL    // Stop processing
    }
}
