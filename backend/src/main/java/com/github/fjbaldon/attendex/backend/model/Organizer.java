package com.github.fjbaldon.attendex.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString(exclude = {"events", "organization", "role"})
@EqualsAndHashCode(exclude = {"events", "organization", "role"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "organizer", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"organization_id", "email"})
})
public class Organizer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @OneToMany(mappedBy = "organizer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Event> events = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    private boolean enabled = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean forcePasswordChange = true;
}
