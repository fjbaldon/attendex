package com.github.fjbaldon.attendex.platform.organization;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

interface OrganizerRepository extends PagingAndSortingRepository<Organizer, Long>, CrudRepository<Organizer, Long> {
    Optional<Organizer> findByEmail(String email);

    boolean existsByOrganizationName(String organizationName);

    Optional<Organizer> findByVerificationToken(String token);

    boolean existsByEmailAndOrganizationId(String email, Long organizationId);

    long countByOrganizationId(Long organizationId);
}
