package com.github.fjbaldon.attendex.backend.service;

import com.github.fjbaldon.attendex.backend.dto.OrganizerResponseDto;
import com.github.fjbaldon.attendex.backend.model.Organizer;
import com.github.fjbaldon.attendex.backend.model.Role;
import com.github.fjbaldon.attendex.backend.repository.EventRepository;
import com.github.fjbaldon.attendex.backend.repository.OrganizerRepository;
import com.github.fjbaldon.attendex.backend.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizerService {

    private final OrganizerRepository organizerRepository;
    private final RoleRepository roleRepository;
    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public Page<OrganizerResponseDto> getOrganizersByOrganization(Long organizationId, Pageable pageable) {
        return organizerRepository.findAllByOrganizationId(organizationId, pageable)
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public OrganizerResponseDto getOrganizerById(Long organizerId, Long organizationId) {
        Organizer organizer = organizerRepository.findByIdAndOrganizationId(organizerId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organizer not found in your organization."));
        return toDto(organizer);
    }

    @Transactional
    public OrganizerResponseDto updateOrganizerRole(Long organizerId, Long newRoleId, Long organizationId) {
        Organizer organizer = organizerRepository.findByIdAndOrganizationId(organizerId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organizer not found in your organization."));

        Role newRole = roleRepository.findByIdAndOrganizationId(newRoleId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found in your organization."));

        boolean isCurrentUserAdmin = "Admin".equalsIgnoreCase(organizer.getRole().getName());
        boolean isNewRoleNotAdmin = !"Admin".equalsIgnoreCase(newRole.getName());

        if (isCurrentUserAdmin && isNewRoleNotAdmin) {
            long adminCount = organizerRepository.countByOrganizationIdAndRole_Name(organizationId, "Admin");
            if (adminCount <= 1) {
                throw new IllegalStateException("Cannot unassign the last admin. The organization must have at least one administrator.");
            }
        }

        organizer.setRole(newRole);
        Organizer updatedOrganizer = organizerRepository.save(organizer);
        return toDto(updatedOrganizer);
    }

    @Transactional
    public void removeOrganizer(Long organizerId, Long organizationId, String currentAdminEmail) {
        Organizer organizerToRemove = organizerRepository.findByIdAndOrganizationId(organizerId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organizer not found in your organization."));

        if (organizerToRemove.getEmail().equals(currentAdminEmail)) {
            throw new IllegalStateException("You cannot remove your own account.");
        }

        if ("Admin".equalsIgnoreCase(organizerToRemove.getRole().getName())) {
            long adminCount = organizerRepository.countByOrganizationIdAndRole_Name(organizationId, "Admin");
            if (adminCount <= 1) {
                throw new IllegalStateException("Cannot remove the last admin from the organization.");
            }
        }

        if (eventRepository.existsByOrganizerId(organizerId)) {
            throw new IllegalStateException("Cannot remove organizer: they are still the owner of one or more events. Please reassign or delete their events first.");
        }

        organizerRepository.delete(organizerToRemove);
    }

    private OrganizerResponseDto toDto(Organizer organizer) {
        return OrganizerResponseDto.builder()
                .id(organizer.getId())
                .email(organizer.getEmail())
                .roleName(organizer.getRole() != null ? organizer.getRole().getName() : null)
                .roleId(organizer.getRole() != null ? organizer.getRole().getId() : null)
                .build();
    }
}
