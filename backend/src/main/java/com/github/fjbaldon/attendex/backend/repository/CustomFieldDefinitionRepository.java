package com.github.fjbaldon.attendex.backend.repository;

import com.github.fjbaldon.attendex.backend.model.CustomFieldDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomFieldDefinitionRepository extends JpaRepository<CustomFieldDefinition, Long> {

    List<CustomFieldDefinition> findByOrganizationId(Long organizationId);

    Optional<CustomFieldDefinition> findByIdAndOrganizationId(Long id, Long organizationId);

    boolean existsByOrganizationIdAndFieldName(Long organizationId, String fieldName);
}
