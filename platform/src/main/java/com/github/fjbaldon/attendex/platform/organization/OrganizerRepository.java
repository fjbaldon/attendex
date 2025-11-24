package com.github.fjbaldon.attendex.platform.organization;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface OrganizerRepository extends PagingAndSortingRepository<Organizer, Long>, CrudRepository<Organizer, Long> {
    Optional<Organizer> findByEmail(String email);

    boolean existsByOrganizationName(String organizationName);

    Optional<Organizer> findByVerificationToken(String token);

    boolean existsByEmailAndOrganizationId(String email, Long organizationId);

    long countByOrganizationId(Long organizationId);

    Page<Organizer> findAllByOrganizationId(Long organizationId, Pageable pageable);

    @Query("SELECT o FROM Organizer o WHERE o.organization.id = :organizationId AND LOWER(o.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Organizer> searchByOrganizationIdAndEmail(@Param("organizationId") Long organizationId, @Param("query") String query, Pageable pageable);
}
