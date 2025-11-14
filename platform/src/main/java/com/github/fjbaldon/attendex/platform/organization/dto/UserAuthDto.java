package com.github.fjbaldon.attendex.platform.organization.dto;

public record UserAuthDto(
        String email,
        String password,
        String role,
        Long organizationId,
        boolean enabled,
        boolean forcePasswordChange
) {
}
