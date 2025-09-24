package com.github.fjbaldon.attendex.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString(exclude = "eventRegistrations")
@EqualsAndHashCode(exclude = "eventRegistrations")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "attendee", indexes = {
        @Index(name = "idx_attendee_lastname", columnList = "lastName")
})
public class Attendee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id_number", unique = true, nullable = false)
    private String schoolIdNumber;

    @Column(nullable = false)
    private String firstName;

    private Character middleInitial;

    @Column(nullable = false)
    private String lastName;

    private String course;

    private Integer yearLevel;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "attendee", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<EventAttendee> eventRegistrations = new HashSet<>();
}
