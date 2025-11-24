package com.github.fjbaldon.attendex.platform.capture;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "capture_orphaned_entry")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class OrphanedEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long organizationId;

    private Long originalEventId;

    private String scanUuid;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> rawPayload;

    private String failureReason;

    private Instant createdAt;

    private OrphanedEntry(Long organizationId, Long originalEventId, String scanUuid, Map<String, Object> rawPayload, String failureReason) {
        Assert.notNull(organizationId, "Organization ID must not be null");
        Assert.hasText(scanUuid, "Scan UUID must not be blank");
        Assert.notNull(rawPayload, "Raw payload must not be null");

        this.organizationId = organizationId;
        this.originalEventId = originalEventId;
        this.scanUuid = scanUuid;
        this.rawPayload = rawPayload;
        this.failureReason = failureReason;
        this.createdAt = Instant.now();
    }

    static OrphanedEntry create(Long organizationId, Long originalEventId, String scanUuid, Map<String, Object> rawPayload, String failureReason) {
        return new OrphanedEntry(organizationId, originalEventId, scanUuid, rawPayload, failureReason);
    }
}
