package com.github.fjbaldon.attendex.platform.organization.dto;

public record OrganizerDto(
        Long id,
        String email,
        boolean enabled,
        boolean forcePasswordChange
) {
}
