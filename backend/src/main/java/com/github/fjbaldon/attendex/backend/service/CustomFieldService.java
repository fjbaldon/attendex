package com.github.fjbaldon.attendex.backend.service;

import com.github.fjbaldon.attendex.backend.dto.CustomFieldDefinitionDto;
import com.github.fjbaldon.attendex.backend.dto.CustomFieldDefinitionRequest;
import com.github.fjbaldon.attendex.backend.model.CustomFieldDefinition;
import com.github.fjbaldon.attendex.backend.model.Organization;
import com.github.fjbaldon.attendex.backend.repository.AttendeeRepository;
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
    private final AttendeeRepository attendeeRepository;

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
    public CustomFieldDefinitionDto updateDefinition(Long fieldId, CustomFieldDefinitionRequest request, Long organizationId) {
        CustomFieldDefinition definition = customFieldDefinitionRepository.findByIdAndOrganizationId(fieldId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Custom field definition not found in your organization."));

        if (!definition.getFieldName().equals(request.getFieldName())) {
            throw new IllegalStateException("Changing the field name is not allowed.");
        }
        if (!definition.getFieldType().equals(request.getFieldType())) {
            throw new IllegalStateException("Changing the field type is not allowed.");
        }

        if (definition.getFieldType() == com.github.fjbaldon.attendex.backend.model.FieldType.SELECT) {
            definition.setOptions(request.getOptions());
        }

        CustomFieldDefinition updated = customFieldDefinitionRepository.save(definition);
        return toDto(updated);
    }

    @Transactional
    public void deleteDefinition(Long fieldId, Long organizationId) {
        CustomFieldDefinition definition = customFieldDefinitionRepository.findByIdAndOrganizationId(fieldId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Custom field definition not found in your organization."));

        boolean isInUse = attendeeRepository.existsByOrganizationIdAndCustomFieldKey(organizationId, definition.getFieldName());
        if (isInUse) {
            throw new IllegalStateException("Cannot delete field '" + definition.getFieldName() + "' because it is currently in use by one or more attendees.");
        }

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
