package com.github.fjbaldon.attendex.platform.admin;

import java.time.Instant;

public record StewardDto(
        Long id,
        String email,
        Instant createdAt
) {
}
