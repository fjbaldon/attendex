package com.github.fjbaldon.attendex.platform.audit;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "audit_audit")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String actorEmail;
    private String actionType;
    private String status;
    private String ipAddress;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> details;

    private Instant createdAt;

    private Audit(String actorEmail, String actionType, String status, String ipAddress, Map<String, Object> details) {
        Assert.hasText(actorEmail, "Actor email must not be blank");
        Assert.hasText(actionType, "Action type must not be blank");
        this.actorEmail = actorEmail;
        this.actionType = actionType;
        this.status = status;
        this.ipAddress = ipAddress;
        this.details = details;
        this.createdAt = Instant.now();
    }

    static Audit record(String actorEmail, String actionType, String status, String ipAddress, Map<String, Object> details) {
        return new Audit(actorEmail, actionType, status, ipAddress, details);
    }
}
