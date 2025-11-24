package com.github.fjbaldon.attendex.platform.analytics;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "analytics_organization_summary")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class OrganizationSummary {

    @Id
    private Long organizationId;

    private long totalEvents;

    private long totalAttendees;

    private long totalScanners;
}
