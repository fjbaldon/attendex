package com.github.fjbaldon.attendex.platform.attendee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface AttendeeRepository extends PagingAndSortingRepository<Attendee, Long>, CrudRepository<Attendee, Long> {

    Page<Attendee> findAllByOrganizationId(Long organizationId, Pageable pageable);

    @Query("SELECT a FROM Attendee a WHERE a.organizationId = :organizationId AND " +
            "(LOWER(a.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(a.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(a.identity) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Attendee> searchByOrganizationId(@Param("organizationId") Long organizationId, @Param("query") String query, Pageable pageable);

    boolean existsByOrganizationIdAndIdentity(Long organizationId, String identity);

    long countByOrganizationId(Long organizationId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM attendee_attendee, jsonb_each(attributes) WHERE key = :attributeName AND organization_id = :organizationId)", nativeQuery = true)
    boolean isAttributeInUse(Long organizationId, String attributeName);

    Optional<Attribute> findByIdAndOrganizationId(Long id, Long organizationId);
}
