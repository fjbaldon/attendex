package com.github.fjbaldon.attendex.backend.service;

import com.github.fjbaldon.attendex.backend.dto.OrganizerResponseDto;
import com.github.fjbaldon.attendex.backend.dto.UserCreateRequestDto;
import com.github.fjbaldon.attendex.backend.model.Organizer;
import com.github.fjbaldon.attendex.backend.model.Organization;
import com.github.fjbaldon.attendex.backend.repository.EventRepository;
import com.github.fjbaldon.attendex.backend.repository.OrganizerRepository;
import com.github.fjbaldon.attendex.backend.repository.ScannerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrganizerService {

    private final OrganizerRepository organizerRepository;
    private final ScannerRepository scannerRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public OrganizerResponseDto createOrganizer(UserCreateRequestDto request, Long organizationId) {
        if (organizerRepository.existsByEmailAndOrganizationId(request.getEmail(), organizationId) ||
                scannerRepository.existsByEmailAndOrganizationId(request.getEmail(), organizationId)) {
            throw new IllegalStateException("A user with this email already exists in the organization.");
        }

        Organization orgReference = new Organization();
        orgReference.setId(organizationId);

        Organizer newUser = Organizer.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getTemporaryPassword()))
                .organization(orgReference)
                .enabled(true)
                .forcePasswordChange(true)
                .build();

        return toDto(organizerRepository.save(newUser));
    }

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
    public void removeOrganizer(Long organizerId, Long organizationId, String currentAdminEmail) {
        Organizer organizerToRemove = organizerRepository.findByIdAndOrganizationId(organizerId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organizer not found in your organization."));

        if (organizerToRemove.getEmail().equals(currentAdminEmail)) {
            throw new IllegalStateException("You cannot remove your own account.");
        }

        if (organizerRepository.countByOrganizationId(organizationId) <= 1) {
            throw new IllegalStateException("Cannot remove the last organizer from the organization.");
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
                .build();
    }
}
