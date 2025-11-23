package com.github.fjbaldon.attendex.platform.organization;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Entity
@Table(name = "organization_scanner")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class Scanner {

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

    private Scanner(String email, String password, Organization organization) {
        Assert.hasText(email, "Email must not be blank");
        Assert.hasText(password, "Password must not be blank");
        Assert.notNull(organization, "Organization must not be null");

        this.email = email;
        this.password = password;
        this.organization = organization;
        this.forcePasswordChange = true;
        this.enabled = true;
    }

    static Scanner create(String email, String encodedPassword, Organization organization) {
        return new Scanner(email, encodedPassword, organization);
    }

    void changePassword(String newEncodedPassword) {
        this.password = newEncodedPassword;
        this.forcePasswordChange = false;
    }

    void requirePasswordChange() {
        this.forcePasswordChange = true;
    }

    void toggleStatus() {
        this.enabled = !this.enabled;
    }
}
