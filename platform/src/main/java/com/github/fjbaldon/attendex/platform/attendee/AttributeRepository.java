package com.github.fjbaldon.attendex.platform.attendee;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface AttributeRepository extends CrudRepository<Attribute, Long> {
    List<Attribute> findAllByOrganizationId(Long organizationId);

    boolean existsByOrganizationIdAndName(Long organizationId, String name);

    Optional<Attribute> findByIdAndOrganizationId(Long id, Long organizationId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM attendee_attendee WHERE organization_id = :organizationId AND jsonb_exists(attributes, :attributeName))", nativeQuery = true)
    boolean isAttributeInUse(@Param("organizationId") Long organizationId, @Param("attributeName") String attributeName);
}
