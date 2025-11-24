package com.github.fjbaldon.attendex.platform.organization;

public record ScannerDto(
        Long id,
        String email,
        boolean enabled,
        boolean forcePasswordChange
) {
}
