package com.github.fjbaldon.attendex.backend.service;

import com.github.fjbaldon.attendex.backend.dto.CustomFieldDefinitionDto;
import com.github.fjbaldon.attendex.backend.dto.CustomFieldDefinitionRequest;
import com.github.fjbaldon.attendex.backend.model.CustomFieldDefinition;
import com.github.fjbaldon.attendex.backend.model.Organization;
import com.github.fjbaldon.attendex.backend.repository.CustomFieldDefinitionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomFieldService {

    private final CustomFieldDefinitionRepository customFieldDefinitionRepository;

    @Transactional(readOnly = true)
    public List<CustomFieldDefinitionDto> getDefinitionsByOrganization(Long organizationId) {
        return customFieldDefinitionRepository.findByOrganizationId(organizationId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CustomFieldDefinitionDto createDefinition(CustomFieldDefinitionRequest request, Long organizationId) {
        if (customFieldDefinitionRepository.existsByOrganizationIdAndFieldName(organizationId, request.getFieldName())) {
            throw new IllegalStateException("A custom field with this name already exists.");
        }

        Organization orgReference = new Organization();
        orgReference.setId(organizationId);

        CustomFieldDefinition definition = CustomFieldDefinition.builder()
                .organization(orgReference)
                .fieldName(request.getFieldName())
                .fieldType(request.getFieldType())
                .options(request.getOptions())
                .build();

        CustomFieldDefinition saved = customFieldDefinitionRepository.save(definition);
        return toDto(saved);
    }

    @Transactional
    public void deleteDefinition(Long fieldId, Long organizationId) {
        CustomFieldDefinition definition = customFieldDefinitionRepository.findByIdAndOrganizationId(fieldId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Custom field definition not found in your organization."));

        // TODO: In a future enhancement, add logic here to check if this field is currently
        // in use by any attendees before allowing deletion.

        customFieldDefinitionRepository.delete(definition);
    }

    private CustomFieldDefinitionDto toDto(CustomFieldDefinition definition) {
        return CustomFieldDefinitionDto.builder()
                .id(definition.getId())
                .fieldName(definition.getFieldName())
                .fieldType(definition.getFieldType())
                .options(definition.getOptions())
                .build();
    }
}
