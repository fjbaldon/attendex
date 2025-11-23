package com.github.fjbaldon.attendex.platform.event;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
class RosterEntryId implements Serializable {
    private Long eventId;
    private Long attendeeId;

    RosterEntryId(Long eventId, Long attendeeId) {
        this.eventId = eventId;
        this.attendeeId = attendeeId;
    }
}
