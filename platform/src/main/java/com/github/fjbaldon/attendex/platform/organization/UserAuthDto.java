package com.github.fjbaldon.attendex.platform.organization;

public record UserAuthDto(
        Long id,
        String email,
        String password,
        String role,
        Long organizationId,
        boolean enabled,
        boolean forcePasswordChange
) {
}
