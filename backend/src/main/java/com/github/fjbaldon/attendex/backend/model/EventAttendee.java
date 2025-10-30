package com.github.fjbaldon.attendex.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@ToString(exclude = {"event", "attendee"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "event_attendee")
public class EventAttendee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendee_id")
    private Attendee attendee;

    @Column(unique = true, nullable = false)
    private String qrCodeHash;

    @PrePersist
    public void prePersist() {
        if (qrCodeHash == null) {
            qrCodeHash = UUID.randomUUID().toString();
        }
    }
}
