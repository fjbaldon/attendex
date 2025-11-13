package com.github.fjbaldon.attendex.platform.organization;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

interface OrganizerRepository extends CrudRepository<Organizer, Long> {

    Optional<Organizer> findByEmail(String email);

    boolean existsByOrganizationName(String organizationName);
}
