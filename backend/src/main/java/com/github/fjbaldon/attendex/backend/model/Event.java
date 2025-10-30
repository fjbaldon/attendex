package com.github.fjbaldon.attendex.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString(exclude = {"organizer", "eventAttendees", "organization", "timeSlots"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "event", indexes = {
        @Index(name = "idx_event_start_date", columnList = "startDate"),
        @Index(name = "idx_event_end_date", columnList = "endDate")
})
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String eventName;

    @Column(nullable = false)
    private Instant startDate;

    @Column(nullable = false)
    private Instant endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private Organizer organizer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<EventAttendee> eventAttendees = new HashSet<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<EventTimeSlot> timeSlots = new HashSet<>();

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
