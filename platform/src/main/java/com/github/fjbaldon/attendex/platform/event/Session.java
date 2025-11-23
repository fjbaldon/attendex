package com.github.fjbaldon.attendex.platform.event;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.Assert;

import java.time.Instant;

@Entity
@Table(name = "event_session")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String activityName;
    private Instant targetTime;
    private String intent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @Setter(AccessLevel.PACKAGE)
    private Event event;

    private Session(String activityName, Instant targetTime, String intent) {
        Assert.hasText(activityName, "Activity name must not be blank");
        Assert.notNull(targetTime, "Target time must not be null");
        Assert.hasText(intent, "Intent must not be blank");

        this.activityName = activityName;
        this.targetTime = targetTime;
        this.intent = intent;
    }

    static Session create(String activityName, Instant targetTime, String intent) {
        return new Session(activityName, targetTime, intent);
    }

    void update(String activityName, Instant targetTime, String intent) {
        Assert.hasText(activityName, "Activity name must not be blank");
        Assert.notNull(targetTime, "Target time must not be null");
        Assert.hasText(intent, "Intent must not be blank");

        this.activityName = activityName;
        this.targetTime = targetTime;
        this.intent = intent;
    }
}
