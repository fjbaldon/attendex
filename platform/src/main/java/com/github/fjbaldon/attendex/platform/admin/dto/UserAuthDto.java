package com.github.fjbaldon.attendex.platform.admin.dto;

public record UserAuthDto(
        String email,
        String password,
        String role,
        boolean forcePasswordChange
) {
}
