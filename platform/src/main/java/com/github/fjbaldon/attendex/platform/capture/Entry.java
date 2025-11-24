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
@Table(name = "capture_entry")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class Entry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String scanUuid;

    private Long organizationId;
    private Long eventId;
    private Long sessionId;
    private Long attendeeId;
    private Long scannerId;
    private Instant scanTimestamp;
    private String punctuality;
    private Instant syncTimestamp;

    @Column(columnDefinition = "TEXT")
    private String snapshotIdentity;

    @Column(columnDefinition = "TEXT")
    private String snapshotFirstName;

    @Column(columnDefinition = "TEXT")
    private String snapshotLastName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> snapshotAttributes;

    private Entry(Long organizationId, Long eventId, Long sessionId, Long attendeeId, Long scannerId,
                  Instant scanTimestamp, String punctuality, String scanUuid,
                  String snapshotIdentity, String snapshotFirstName, String snapshotLastName,
                  Map<String, Object> snapshotAttributes) {

        Assert.notNull(organizationId, "Organization ID must not be null");
        Assert.notNull(eventId, "Event ID must not be null");
        // CHANGED: Removed assertion for sessionId (It can now be null for Unscheduled/Orphaned entries)
        Assert.notNull(attendeeId, "Attendee ID must not be null");
        Assert.notNull(scannerId, "Scanner ID must not be null");
        Assert.notNull(scanTimestamp, "Scan timestamp must not be null");
        Assert.hasText(punctuality, "Punctuality must not be blank");

        this.organizationId = organizationId;
        this.eventId = eventId;
        this.sessionId = sessionId;
        this.attendeeId = attendeeId;
        this.scannerId = scannerId;
        this.scanTimestamp = scanTimestamp;
        this.punctuality = punctuality;
        this.scanUuid = scanUuid;
        this.syncTimestamp = Instant.now();

        this.snapshotIdentity = snapshotIdentity;
        this.snapshotFirstName = snapshotFirstName;
        this.snapshotLastName = snapshotLastName;
        this.snapshotAttributes = snapshotAttributes;
    }

    static Entry create(Long organizationId, Long eventId, Long sessionId, Long attendeeId, Long scannerId,
                        Instant scanTimestamp, String punctuality, String scanUuid,
                        String snapshotIdentity, String snapshotFirstName, String snapshotLastName,
                        Map<String, Object> snapshotAttributes) {
        return new Entry(organizationId, eventId, sessionId, attendeeId, scannerId,
                scanTimestamp, punctuality, scanUuid,
                snapshotIdentity, snapshotFirstName, snapshotLastName, snapshotAttributes);
    }

    // NEW: Logic to move an entry to a different session (or NULL)
    void reassignToSession(Long newSessionId, String newPunctuality) {
        this.sessionId = newSessionId;
        this.punctuality = newPunctuality;
    }
}
