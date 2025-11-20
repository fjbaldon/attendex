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

    private RosterEntry(Event event, Long attendeeId) {
        Assert.notNull(event, "Event must not be null");
        Assert.notNull(attendeeId, "Attendee ID must not be null");

        this.id = new RosterEntryId(event.getId(), attendeeId);
        this.event = event;
    }

    static RosterEntry create(Event event, Long attendeeId) {
        return new RosterEntry(event, attendeeId);
    }
}
