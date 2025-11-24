package com.github.fjbaldon.attendex.platform.analytics;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "analytics_scanner_summary")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class ScannerSummary {
    @Id
    private Long scannerId;
    private Long eventId;
    private long entryCount;
}
