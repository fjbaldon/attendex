package com.github.fjbaldon.attendex.platform.organization;

import com.github.fjbaldon.attendex.platform.organization.dto.OrganizationDto;
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

    private Organization(String name) {
        Assert.hasText(name, "Organization name must not be blank");
        this.name = name;
        this.lifecycle = "INACTIVE";
        this.subscriptionType = "TRIAL";
        this.subscriptionExpiresAt = Instant.now().plus(30, ChronoUnit.DAYS);
    }

    static Organization register(String name) {
        return new Organization(name);
    }

    OrganizationDto toDto() {
        return new OrganizationDto(
                this.id,
                this.name,
                this.lifecycle,
                this.subscriptionType,
                this.subscriptionExpiresAt
        );
    }
}
