package com.github.fjbaldon.attendex.platform.organization;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class OrganizationIngestService {

    private final OrganizationRepository organizationRepository;
    private final OrganizerRepository organizerRepository;
    private final ScannerRepository scannerRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public OrganizationDto registerOrganization(RegistrationRequestDto request) {
        Assert.isTrue(!organizerRepository.existsByOrganizationName(request.organizationName()), "Organization name is already taken");

        assertEmailIsGloballyUnique(request.email());

        Organization organization = Organization.register(request.organizationName());
        Organization savedOrganization = organizationRepository.save(organization);

        String encodedPassword = passwordEncoder.encode(request.password());
        String token = UUID.randomUUID().toString();
        Instant expiry = Instant.now().plus(24, ChronoUnit.HOURS);

        Organizer organizer = Organizer.create(request.email(), encodedPassword, savedOrganization, token, expiry);
        organizerRepository.save(organizer);

        eventPublisher.publishEvent(new OrganizationRegisteredEvent(
                organizer.getEmail(),
                savedOrganization.getName(),
                organizer.getVerificationToken()
        ));

        return savedOrganization.toDto();
    }

    @Transactional
    public void verifyOrganizer(String token) {
        Organizer organizer = organizerRepository.findByVerificationToken(token)
                .orElseThrow(() -> new EntityNotFoundException("Invalid verification token."));

        Assert.isTrue(organizer.getTokenExpiryDate().isAfter(Instant.now()), "Verification token has expired.");

        organizer.verify();
        organizerRepository.save(organizer);

        Organization organization = organizer.getOrganization();
        if ("INACTIVE".equals(organization.getLifecycle())) {
            organization.updateLifecycle("ACTIVE");
            organizationRepository.save(organization);
        }
    }

    @Transactional
    public OrganizationDto updateLifecycle(Long organizationId, String lifecycle) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found with ID: " + organizationId));
        organization.updateLifecycle(lifecycle);
        return organizationRepository.save(organization).toDto();
    }

    @Transactional
    public OrganizationDto updateSubscription(Long organizationId, String subscriptionType, Instant expiresAt) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found with ID: " + organizationId));
        organization.updateSubscription(subscriptionType, expiresAt);
        return organizationRepository.save(organization).toDto();
    }

    @Transactional
    public OrganizationDto updateOrganizationDetails(Long organizationId, UpdateOrganizationDetailsDto dto) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));
        organization.updateDetails(dto.name(), dto.identityFormatRegex());
        return organization.toDto();
    }

    private void assertEmailIsGloballyUnique(String email) {
        boolean existsAsOrganizer = organizerRepository.findByEmail(email).isPresent();
        boolean existsAsScanner = scannerRepository.findByEmail(email).isPresent();
        Assert.isTrue(!existsAsOrganizer && !existsAsScanner, "This email is already in use by another user.");
    }
}
