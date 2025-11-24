package com.github.fjbaldon.attendex.platform.attendee;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface AttendeeRepository extends PagingAndSortingRepository<Attendee, Long>, CrudRepository<Attendee, Long>, JpaSpecificationExecutor<Attendee> {

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Attendee a WHERE a.organizationId = :organizationId AND a.identity = :identity AND a.deletedAt IS NULL")
    boolean existsByOrganizationIdAndIdentity(@Param("organizationId") Long organizationId, @Param("identity") String identity);

    @Query("SELECT a FROM Attendee a WHERE a.organizationId = :organizationId AND a.identity = :identity AND a.deletedAt IS NULL")
    Attendee findAttendeeByIdentity(@Param("organizationId") Long organizationId, @Param("identity") String identity);

    @Query("SELECT a FROM Attendee a WHERE a.organizationId = :orgId AND a.identity = :identity")
    Optional<Attendee> findAnyAttendeeByIdentity(@Param("orgId") Long orgId, @Param("identity") String identity);

    @Query(value = "SELECT id FROM attendee_attendee WHERE organization_id = :orgId AND deleted_at IS NULL AND attributes ->> :key = :value", nativeQuery = true)
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

    @Modifying
    @Query("UPDATE Attendee a SET a.deletedAt = CURRENT_TIMESTAMP WHERE a.id IN :ids AND a.organizationId = :organizationId")
    int softDeleteBatch(@Param("organizationId") Long organizationId, @Param("ids") List<Long> ids);
}
