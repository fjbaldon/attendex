package com.github.fjbaldon.attendex.backend.service;

import com.github.fjbaldon.attendex.backend.dto.OrganizationResponseDto;
import com.github.fjbaldon.attendex.backend.dto.OrganizationUpdateRequest;
import com.github.fjbaldon.attendex.backend.model.Organization;
import com.github.fjbaldon.attendex.backend.repository.OrganizationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    @Transactional(readOnly = true)
    public OrganizationResponseDto getOrganizationDtoById(Long organizationId) {
        return organizationRepository.findById(organizationId)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));
    }

    @Transactional
    public OrganizationResponseDto updateOrganization(Long organizationId, OrganizationUpdateRequest request) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

        organization.setName(request.getName());
        organization.setIdentifierFormatRegex(request.getIdentifierFormatRegex());

        Organization savedOrganization = organizationRepository.save(organization);
        return toDto(savedOrganization);
    }

    private OrganizationResponseDto toDto(Organization organization) {
        return OrganizationResponseDto.builder()
                .id(organization.getId())
                .name(organization.getName())
                .identifierFormatRegex(organization.getIdentifierFormatRegex())
                .build();
    }
}
