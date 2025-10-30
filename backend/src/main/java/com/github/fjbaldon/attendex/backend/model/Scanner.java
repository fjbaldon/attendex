package com.github.fjbaldon.attendex.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@ToString(exclude = {"organization"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "scanner", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"organization_id", "email"})
})
public class Scanner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private boolean enabled = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean forcePasswordChange = true;
}
