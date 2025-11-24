package com.github.fjbaldon.attendex.platform.notification;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_notification_outbox")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class NotificationOutbox {

    enum Status {
        PENDING,
        PROCESSED,
        FAILED
    }

    @Id
    private UUID id;

    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String payload;

    @Enumerated(EnumType.STRING)
    private Status status;

    private int retryCount;

    private Instant lastAttemptAt;

    private Instant createdAt;

    private NotificationOutbox(String eventType, String payload) {
        Assert.hasText(eventType, "Event type must not be blank");
        Assert.notNull(payload, "Payload must not be null");

        this.id = UUID.randomUUID();
        this.eventType = eventType;
        this.payload = payload;
        this.status = Status.PENDING;
        this.retryCount = 0;
        this.createdAt = Instant.now();
    }

    static NotificationOutbox create(Object event, String payload) {
        return new NotificationOutbox(event.getClass().getName(), payload);
    }

    void markAsProcessed() {
        this.status = Status.PROCESSED;
        this.lastAttemptAt = Instant.now();
    }

    void incrementRetryCountAndMarkAsFailed() {
        this.status = Status.FAILED;
        this.retryCount++;
        this.lastAttemptAt = Instant.now();
    }
}
