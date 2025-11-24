package com.github.fjbaldon.attendex.platform.capture;

import java.time.Instant;

public record RecentActivityDto(
        String attendeeName,
        String eventName,
        Instant scanTime,
        String punctuality
) {
}
