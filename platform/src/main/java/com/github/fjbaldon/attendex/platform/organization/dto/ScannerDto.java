package com.github.fjbaldon.attendex.platform.organization.dto;

public record ScannerDto(
        Long id,
        String email,
        boolean enabled,
        boolean forcePasswordChange
) {
}
