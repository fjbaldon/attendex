package com.github.fjbaldon.attendex.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@ToString(exclude = {"event", "attendee", "scanner"})
@EqualsAndHashCode(exclude = {"event", "attendee", "scanner"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "attendance_record", indexes = {
        @Index(name = "idx_record_event_id", columnList = "event_id"),
        @Index(name = "idx_record_attendee_id", columnList = "attendee_id")
})
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendee_id", nullable = false)
    private Attendee attendee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scanner_id", nullable = false)
    private Scanner scanner;

    @Column(nullable = false)
    private Instant checkInTimestamp;

    @Column(nullable = false)
    private Instant syncTimestamp = Instant.now();
}
