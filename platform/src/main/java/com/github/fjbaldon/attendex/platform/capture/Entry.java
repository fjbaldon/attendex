package com.github.fjbaldon.attendex.platform.capture;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.time.Instant;

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

    private String snapshotIdentity;
    private String snapshotFirstName;
    private String snapshotLastName;

    private Entry(Long organizationId, Long eventId, Long sessionId, Long attendeeId, Long scannerId,
                  Instant scanTimestamp, String punctuality, String scanUuid,
                  String snapshotIdentity, String snapshotFirstName, String snapshotLastName) {

        Assert.notNull(organizationId, "Organization ID must not be null");
        Assert.notNull(eventId, "Event ID must not be null");
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
    }

    static Entry create(Long organizationId, Long eventId, Long sessionId, Long attendeeId, Long scannerId,
                        Instant scanTimestamp, String punctuality, String scanUuid,
                        String snapshotIdentity, String snapshotFirstName, String snapshotLastName) {
        return new Entry(organizationId, eventId, sessionId, attendeeId, scannerId,
                scanTimestamp, punctuality, scanUuid,
                snapshotIdentity, snapshotFirstName, snapshotLastName);
    }
}
