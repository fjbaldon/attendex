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

    private Long sessionId;
    private Long attendeeId;
    private Long scannerId;
    private Instant scanTimestamp;
    private String punctuality;
    private Instant syncTimestamp;

    private Entry(Long sessionId, Long attendeeId, Long scannerId, Instant scanTimestamp, String punctuality) {
        Assert.notNull(sessionId, "Session ID must not be null");
        Assert.notNull(attendeeId, "Attendee ID must not be null");
        Assert.notNull(scannerId, "Scanner ID must not be null");
        Assert.notNull(scanTimestamp, "Scan timestamp must not be null");
        Assert.hasText(punctuality, "Punctuality must not be blank");

        this.sessionId = sessionId;
        this.attendeeId = attendeeId;
        this.scannerId = scannerId;
        this.scanTimestamp = scanTimestamp;
        this.punctuality = punctuality;
        this.syncTimestamp = Instant.now();
    }

    static Entry create(Long sessionId, Long attendeeId, Long scannerId, Instant scanTimestamp, String punctuality) {
        return new Entry(sessionId, attendeeId, scannerId, scanTimestamp, punctuality);
    }
}
