package com.github.fjbaldon.attendex.platform.organization;

public record OrganizerDto(
        Long id,
        String email,
        boolean enabled,
        boolean forcePasswordChange
) {
}
