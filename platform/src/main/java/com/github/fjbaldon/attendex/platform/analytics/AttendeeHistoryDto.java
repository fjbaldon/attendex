package com.github.fjbaldon.attendex.platform.analytics;

import com.github.fjbaldon.attendex.platform.attendee.AttendeeDto;

import java.util.List;

public record AttendeeHistoryDto(
        AttendeeDto profile,
        int totalEvents,
        int totalAttended,
        int totalAbsent,
        double attendanceRate,
        List<AttendeeHistoryItemDto> history
) {
}
