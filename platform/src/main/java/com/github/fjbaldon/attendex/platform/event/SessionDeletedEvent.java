package com.github.fjbaldon.attendex.platform.event;

import java.util.List;

public record SessionDeletedEvent(
        Long deletedSessionId,
        List<SessionDetailsDto> remainingSessions
) {
}
