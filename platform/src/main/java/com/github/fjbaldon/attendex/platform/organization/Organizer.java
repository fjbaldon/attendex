package com.github.fjbaldon.attendex.platform.organization;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.time.Instant;

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

    private String verificationToken;
    private Instant tokenExpiryDate;

    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    private Organizer(String email, String password, Organization organization, String verificationToken, Instant tokenExpiryDate) {
        Assert.hasText(email, "Email must not be blank");
        Assert.hasText(password, "Password must not be blank");
        Assert.notNull(organization, "Organization must not be null");

        this.email = email;
        this.password = password;
        this.organization = organization;
        this.verificationToken = verificationToken;
        this.tokenExpiryDate = tokenExpiryDate;
        this.enabled = (verificationToken == null);
        this.forcePasswordChange = (verificationToken == null);
    }

    static Organizer create(String email, String encodedPassword, Organization organization, String token, Instant expiry) {
        return new Organizer(email, encodedPassword, organization, token, expiry);
    }

    void verify() {
        this.enabled = true;
        this.verificationToken = null;
        this.tokenExpiryDate = null;
    }

    void changePassword(String newEncodedPassword) {
        this.password = newEncodedPassword;
        this.forcePasswordChange = false;
    }
}
