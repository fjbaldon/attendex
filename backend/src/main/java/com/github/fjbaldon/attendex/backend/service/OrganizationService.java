package com.github.fjbaldon.attendex.backend.service;

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
    public Organization getOrganizationById(Long organizationId) {
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));
    }

    @Transactional
    public Organization updateOrganization(Long organizationId, OrganizationUpdateRequest request) {
        Organization organization = getOrganizationById(organizationId);
        organization.setName(request.getName());
        organization.setIdentifierFormatRegex(request.getIdentifierFormatRegex());
        return organizationRepository.save(organization);
    }
}
