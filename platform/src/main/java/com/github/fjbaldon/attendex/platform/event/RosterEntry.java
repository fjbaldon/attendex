package com.github.fjbaldon.attendex.platform.event;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Entity
@Table(name = "event_rosterentry")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class RosterEntry {

    @EmbeddedId
    private RosterEntryId id;

    @ManyToOne
    @MapsId("eventId")
    @JoinColumn(name = "event_id")
    private Event event;

    private String qrCodeHash;

    private RosterEntry(Event event, Long attendeeId, String qrCodeHash) {
        Assert.notNull(event, "Event must not be null");
        Assert.notNull(attendeeId, "Attendee ID must not be null");
        Assert.hasText(qrCodeHash, "QR code hash must not be blank");

        this.id = new RosterEntryId(event.getId(), attendeeId);
        this.event = event;
        this.qrCodeHash = qrCodeHash;
    }

    static RosterEntry create(Event event, Long attendeeId, String qrCodeHash) {
        return new RosterEntry(event, attendeeId, qrCodeHash);
    }
}
