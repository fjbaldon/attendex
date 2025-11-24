package com.github.fjbaldon.attendex.platform.attendee;

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
@Table(name = "attendee_attendee")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class Attendee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long organizationId;

    @Column(nullable = false)
    private String identity;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> attributes;

    @Column(nullable = false)
    private Instant createdAt;

    // NEW: Soft Delete Field
    private Instant deletedAt;

    private Attendee(Long organizationId, String identity, String firstName, String lastName, Map<String, Object> attributes) {
        Assert.notNull(organizationId, "Organization ID must not be null");
        Assert.hasText(identity, "Identity must not be blank");
        Assert.hasText(firstName, "First name must not be blank");
        Assert.hasText(lastName, "Last name must not be blank");

        this.organizationId = organizationId;
        this.identity = identity;
        this.firstName = firstName;
        this.lastName = lastName;
        this.attributes = attributes;
        this.createdAt = Instant.now();
    }

    static Attendee create(Long organizationId, String identity, String firstName, String lastName, Map<String, Object> attributes) {
        return new Attendee(organizationId, identity, firstName, lastName, attributes);
    }

    void update(String newFirstName, String newLastName, Map<String, Object> newAttributes) {
        Assert.hasText(newFirstName, "First name must not be blank");
        Assert.hasText(newLastName, "Last name must not be blank");
        this.firstName = newFirstName;
        this.lastName = newLastName;
        this.attributes = newAttributes;
    }

    void markAsDeleted() {
        this.deletedAt = Instant.now();
    }

    boolean isActive() {
        return this.deletedAt == null;
    }

    void reactivate() {
        this.deletedAt = null;
    }
}
