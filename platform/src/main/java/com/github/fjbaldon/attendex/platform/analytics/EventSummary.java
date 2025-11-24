package com.github.fjbaldon.attendex.platform.analytics;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "analytics_event_summary")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class EventSummary {
    @Id
    private Long eventId;
    private Long organizationId;
    private String eventName;
    private long rosterCount;
    private long entryCount;

    EventSummary(Long eventId, Long organizationId, String eventName) {
        this.eventId = eventId;
        this.organizationId = organizationId;
        this.eventName = eventName;
        this.rosterCount = 0;
        this.entryCount = 0;
    }
}
