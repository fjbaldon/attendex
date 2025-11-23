package com.github.fjbaldon.attendex.platform.capture.events;

import java.util.List;
import java.util.Map;

public record EntriesSyncedEvent(
        Map<Long, List<Long>> attendeeIdsByEventId
) {
}
