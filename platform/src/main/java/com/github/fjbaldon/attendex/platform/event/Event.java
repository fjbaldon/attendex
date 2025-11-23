package com.github.fjbaldon.attendex.platform.event;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "event_event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long organizationId;
    private Long organizerId;
    private String name;
    private Instant startDate;
    private Instant endDate;
    private int graceMinutesBefore;
    private int graceMinutesAfter;
    private Instant createdAt;

    private Instant deletedAt;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<Session> sessions = new HashSet<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<RosterEntry> rosterEntries = new HashSet<>();

    private Event(Long organizationId, Long organizerId, String name, Instant startDate, Instant endDate, int graceMinutesBefore, int graceMinutesAfter) {
        Assert.notNull(organizationId, "Organization ID must not be null");
        Assert.notNull(organizerId, "Organizer ID must not be null");
        Assert.hasText(name, "Event name must not be blank");
        Assert.notNull(startDate, "Start date must not be null");
        Assert.notNull(endDate, "End date must not be null");
        Assert.isTrue(!endDate.isBefore(startDate), "End date must not be before start date");

        this.organizationId = organizationId;
        this.organizerId = organizerId;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.graceMinutesBefore = graceMinutesBefore;
        this.graceMinutesAfter = graceMinutesAfter;
        this.createdAt = Instant.now();
    }

    static Event create(Long organizationId, Long organizerId, String name, Instant startDate, Instant endDate, int graceBefore, int graceAfter) {
        return new Event(organizationId, organizerId, name, startDate, endDate, graceBefore, graceAfter);
    }

    void addSession(Session session) {
        this.sessions.add(session);
        session.setEvent(this);
    }

    void updateDetails(String name, Instant startDate, Instant endDate, int graceMinutesBefore, int graceMinutesAfter) {
        Assert.hasText(name, "Event name must not be blank");
        Assert.notNull(startDate, "Start date must not be null");
        Assert.notNull(endDate, "End date must not be null");
        Assert.isTrue(!endDate.isBefore(startDate), "End date must not be before start date");

        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.graceMinutesBefore = graceMinutesBefore;
        this.graceMinutesAfter = graceMinutesAfter;
    }

    void removeSession(Session session) {
        this.sessions.remove(session);
        session.setEvent(null);
    }

    void markAsDeleted() {
        this.deletedAt = Instant.now();
    }
}
