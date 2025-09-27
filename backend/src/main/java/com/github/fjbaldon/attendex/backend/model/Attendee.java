package com.github.fjbaldon.attendex.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
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
        @Index(name = "idx_attendee_lastname", columnList = "lastName"),
        @Index(name = "uk_attendee_org_identifier", columnList = "organization_id, unique_identifier", unique = true)
})
public class Attendee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "unique_identifier", nullable = false)
    private String uniqueIdentifier;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> customFields;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "attendee", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<EventAttendee> eventRegistrations = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;
}
