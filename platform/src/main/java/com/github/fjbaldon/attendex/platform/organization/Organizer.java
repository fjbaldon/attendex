package com.github.fjbaldon.attendex.platform.organization;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Entity
@Table(name = "organization_organizer")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class Organizer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String password;

    private boolean forcePasswordChange;

    private boolean enabled;

    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    private Organizer(String email, String password, Organization organization) {
        Assert.hasText(email, "Email must not be blank");
        Assert.hasText(password, "Password must not be blank");
        Assert.notNull(organization, "Organization must not be null");

        this.email = email;
        this.password = password;
        this.organization = organization;
        this.forcePasswordChange = false;
        this.enabled = true; // For simplicity, we enable the first organizer immediately.
    }

    static Organizer create(String email, String encodedPassword, Organization organization) {
        return new Organizer(email, encodedPassword, organization);
    }
}
