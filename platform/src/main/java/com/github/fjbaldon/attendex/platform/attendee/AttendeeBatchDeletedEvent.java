package com.github.fjbaldon.attendex.platform.attendee;

public record AttendeeBatchDeletedEvent(
        Long organizationId,
        int count
) {}
