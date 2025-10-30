package com.github.fjbaldon.attendex.backend.dto;

import com.github.fjbaldon.attendex.backend.model.TimeSlotType;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class AttendanceSyncRequest {

    private List<Record> records;

    @Data
    public static class Record {
        private Long eventId;
        private Long attendeeId;
        private Instant checkInTimestamp;
        private TimeSlotType type;
    }
}
