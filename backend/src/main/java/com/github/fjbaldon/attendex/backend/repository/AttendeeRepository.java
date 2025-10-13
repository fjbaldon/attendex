package com.github.fjbaldon.attendex.backend.repository;

import com.github.fjbaldon.attendex.backend.model.Attendee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendeeRepository extends JpaRepository<Attendee, Long> {
    Page<Attendee> findAllByOrganizationId(Long organizationId, Pageable pageable);

    @Query(value = "SELECT DISTINCT key FROM attendee, jsonb_object_keys(custom_fields) as key WHERE organization_id = :organizationId", nativeQuery = true)
    List<String> findDistinctCustomFieldKeysByOrganizationId(Long organizationId);

    long countByOrganizationId(Long organizationId);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM attendee WHERE organization_id = :organizationId AND jsonb_exists(custom_fields, :fieldName))", nativeQuery = true)
    boolean existsByOrganizationIdAndCustomFieldKey(Long organizationId, String fieldName);
}
