package com.github.fjbaldon.attendex.platform.admin;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.time.Instant;

@Entity
@Table(name = "admin_steward")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class Steward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String password;

    private Instant createdAt;

    private boolean forcePasswordChange;

    private Steward(String email, String password) {
        Assert.hasText(email, "Email must not be blank");
        Assert.hasText(password, "Password must not be blank");
        this.email = email;
        this.password = password;
        this.createdAt = Instant.now();
        this.forcePasswordChange = true;
    }

    static Steward create(String email, String encodedPassword) {
        return new Steward(email, encodedPassword);
    }

    void changePassword(String newEncodedPassword) {
        this.password = newEncodedPassword;
        this.forcePasswordChange = false;
    }
}
