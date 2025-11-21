package com.github.fjbaldon.attendex.platform.analytics;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "analytics_organizationsummary")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class OrganizationSummary {
    @Id
    private Long organizationId;
    private long totalEvents;
    private long totalAttendees;
    private long totalScanners;

    OrganizationSummary(Long organizationId) {
        this.organizationId = organizationId;
        this.totalEvents = 0;
        this.totalAttendees = 0;
        this.totalScanners = 0;
    }

    void incrementEventCount() {
        this.totalEvents++;
    }

    void incrementAttendeeCount() {
        this.totalAttendees++;
    }

    // IMPLEMENTED: Decrement logic with safety check
    void decrementAttendeeCount() {
        if (this.totalAttendees > 0) {
            this.totalAttendees--;
        }
    }

    void incrementScannerCount() {
        this.totalScanners++;
    }
}
