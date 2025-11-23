package com.github.fjbaldon.attendex.platform.attendee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface AttendeeRepository extends PagingAndSortingRepository<Attendee, Long>, CrudRepository<Attendee, Long> {

    Page<Attendee> findAllByOrganizationId(Long organizationId, Pageable pageable);

    @Query("SELECT a FROM Attendee a WHERE a.organizationId = :organizationId AND " +
            "(LOWER(a.identity) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(a.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(a.lastName) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Attendee> searchByOrganizationId(@Param("organizationId") Long organizationId, @Param("query") String query, Pageable pageable);

    boolean existsByOrganizationIdAndIdentity(Long organizationId, String identity);

    @Query("SELECT a FROM Attendee a WHERE a.organizationId = :organizationId AND a.identity = :identity")
    Attendee findAttendeeByIdentity(@Param("organizationId") Long organizationId, @Param("identity") String identity);

    @Query(value = "SELECT id FROM attendee_attendee WHERE organization_id = :orgId AND attributes ->> :key = :value", nativeQuery = true)
    List<Long> findIdsByAttributeValue(@Param("orgId") Long orgId, @Param("key") String key, @Param("value") String value);

    @Modifying
    @Query(nativeQuery = true, value = """
        UPDATE attendee_attendee
        SET attributes = (attributes - :oldName) || jsonb_build_object(:newName, attributes->:oldName)
        WHERE organization_id = :orgId
          AND jsonb_exists(attributes, :oldName)
    """)
    void renameAttributeKey(@Param("orgId") Long orgId, @Param("oldName") String oldName, @Param("newName") String newName);

    @Modifying
    @Query(nativeQuery = true, value = "UPDATE attendee_attendee SET attributes = attributes - :attributeName WHERE organization_id = :orgId")
    void removeAttributeFromAllAttendees(@Param("orgId") Long orgId, @Param("attributeName") String attributeName);
}
