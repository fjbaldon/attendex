package com.github.fjbaldon.attendex.platform.organization;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "organization_organization")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String identityFormatRegex;
    private String lifecycle;
    private String subscriptionType;
    private Instant subscriptionExpiresAt;

    @Column(nullable = false)
    private Instant createdAt;

    private Organization(String name) {
        Assert.hasText(name, "Organization name must not be blank");
        this.name = name;
        this.lifecycle = "INACTIVE";
        this.subscriptionType = "TRIAL";
        this.subscriptionExpiresAt = Instant.now().plus(30, ChronoUnit.DAYS);
        this.createdAt = Instant.now();
    }

    void updateLifecycle(String newLifecycle) {
        Assert.hasText(newLifecycle, "Lifecycle cannot be blank");
        this.lifecycle = newLifecycle;
    }

    void updateSubscription(String newSubscriptionType, Instant newExpiresAt) {
        Assert.hasText(newSubscriptionType, "Subscription type cannot be blank");
        this.subscriptionType = newSubscriptionType;
        this.subscriptionExpiresAt = newExpiresAt;
    }

    void updateDetails(String newName, String newIdentityFormatRegex) {
        Assert.hasText(newName, "Organization name must not be blank");
        this.name = newName;
        this.identityFormatRegex = newIdentityFormatRegex;
    }

    static Organization register(String name) {
        return new Organization(name);
    }

    OrganizationDto toDto() {
        return new OrganizationDto(
                this.id,
                this.name,
                this.identityFormatRegex,
                this.lifecycle,
                this.subscriptionType,
                this.subscriptionExpiresAt,
                this.createdAt
        );
    }
}
