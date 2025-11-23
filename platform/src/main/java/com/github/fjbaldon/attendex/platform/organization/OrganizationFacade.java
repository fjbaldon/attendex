package com.github.fjbaldon.attendex.platform.organization;

import com.github.fjbaldon.attendex.platform.organization.dto.*;
import com.github.fjbaldon.attendex.platform.organization.events.OrganizationRegisteredEvent;
import com.github.fjbaldon.attendex.platform.organization.events.ScannerCreatedEvent;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class OrganizationFacade {

    private final OrganizationRepository organizationRepository;
    private final OrganizerRepository organizerRepository;
    private final ScannerRepository scannerRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public OrganizationDto registerOrganization(RegistrationRequestDto request) {
        Assert.isTrue(!organizerRepository.existsByOrganizationName(request.organizationName()), "Organization name is already taken");
        Assert.isTrue(organizerRepository.findByEmail(request.email()).isEmpty(), "Email is already in use");

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
    public OrganizerDto createOrganizer(Long organizationId, CreateUserRequestDto request) {
        assertEmailIsUniqueInOrganization(request.email(), organizationId);
        Organization organization = getOrganizationReference(organizationId);

        String encodedPassword = passwordEncoder.encode(request.password());
        Organizer organizer = Organizer.create(request.email(), encodedPassword, organization, null, null);

        Organizer saved = organizerRepository.save(organizer);
        eventPublisher.publishEvent(new ScannerCreatedEvent(saved.getId(), saved.getOrganization().getId()));
        return toOrganizerDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<OrganizerDto> findOrganizers(Long organizationId, Pageable pageable) {
        return organizerRepository.findAll(pageable).map(this::toOrganizerDto);
    }

    @Transactional
    public void deleteOrganizer(Long organizationId, Long organizerIdToDelete, String currentOrganizerEmail) {
        Organizer organizer = organizerRepository.findById(organizerIdToDelete)
                .orElseThrow(() -> new EntityNotFoundException("Organizer not found"));

        Assert.isTrue(organizer.getOrganization().getId().equals(organizationId), "Organizer does not belong to this organization.");
        Assert.isTrue(!organizer.getEmail().equals(currentOrganizerEmail), "You cannot delete your own account.");
        Assert.isTrue(organizerRepository.countByOrganizationId(organizationId) > 1, "Cannot delete the last organizer.");

        organizerRepository.delete(organizer);
    }

    @Transactional
    public ScannerDto createScanner(Long organizationId, CreateUserRequestDto request) {
        assertEmailIsUniqueInOrganization(request.email(), organizationId);
        Organization organization = getOrganizationReference(organizationId);

        String encodedPassword = passwordEncoder.encode(request.password());
        Scanner scanner = Scanner.create(request.email(), encodedPassword, organization);

        Scanner saved = scannerRepository.save(scanner);
        eventPublisher.publishEvent(new ScannerCreatedEvent(saved.getId(), saved.getOrganization().getId()));
        return toScannerDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<ScannerDto> findScanners(Long organizationId, Pageable pageable) {
        return scannerRepository.findAll(pageable).map(this::toScannerDto);
    }

    @Transactional
    public void deleteScanner(Long organizationId, Long scannerId) {
        Scanner scanner = scannerRepository.findById(scannerId)
                .orElseThrow(() -> new EntityNotFoundException("Scanner not found"));

        Assert.isTrue(scanner.getOrganization().getId().equals(organizationId), "Scanner does not belong to this organization.");

        scannerRepository.delete(scanner);
    }

    @Transactional(readOnly = true)
    public Optional<UserAuthDto> findUserAuthByEmail(String email) {
        Optional<Organizer> organizerOpt = organizerRepository.findByEmail(email);
        if (organizerOpt.isPresent()) {
            Organizer organizer = organizerOpt.get();
            return Optional.of(new UserAuthDto(
                    organizer.getId(),
                    organizer.getEmail(),
                    organizer.getPassword(),
                    "ROLE_ORGANIZER",
                    organizer.getOrganization().getId(),
                    organizer.isEnabled(),
                    organizer.isForcePasswordChange()
            ));
        }

        Optional<Scanner> scannerOpt = scannerRepository.findByEmail(email);
        if (scannerOpt.isPresent()) {
            Scanner scanner = scannerOpt.get();
            return Optional.of(new UserAuthDto(
                    scanner.getId(),
                    scanner.getEmail(),
                    scanner.getPassword(),
                    "ROLE_SCANNER",
                    scanner.getOrganization().getId(),
                    scanner.isEnabled(),
                    scanner.isForcePasswordChange()
            ));
        }

        return Optional.empty();
    }

    @Transactional
    public void resetUserPassword(Long organizationId, Long userId, String newPassword) {
        String encodedPassword = passwordEncoder.encode(newPassword);

        organizerRepository.findById(userId)
                .filter(user -> user.getOrganization().getId().equals(organizationId))
                .ifPresentOrElse(
                        organizer -> organizer.changePassword(encodedPassword),
                        () -> {
                            Scanner scanner = scannerRepository.findById(userId)
                                    .filter(user -> user.getOrganization().getId().equals(organizationId))
                                    .orElseThrow(() -> new EntityNotFoundException("User not found in this organization"));
                            scanner.changePassword(encodedPassword);
                            scanner.requirePasswordChange();
                        }
                );
    }

    @Transactional
    public OrganizationDto updateOrganizationDetails(Long organizationId, UpdateOrganizationDetailsDto dto) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));
        organization.updateDetails(dto.name(), dto.identityFormatRegex());
        return organization.toDto();
    }

    @Transactional(readOnly = true)
    public OrganizationDto findOrganizationById(Long organizationId) {
        return organizationRepository.findById(organizationId)
                .map(Organization::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));
    }

    @Transactional(readOnly = true)
    public Optional<ScannerAuthDto> findScannerAuthByEmail(String email) {
        return scannerRepository.findByEmail(email)
                .map(scanner -> new ScannerAuthDto(scanner.getId(), scanner.getOrganization().getId()));
    }

    @Transactional(readOnly = true)
    public long countScanners(Long organizationId) {
        return scannerRepository.countByOrganizationId(organizationId);
    }

    @Transactional
    public void changeUserPassword(String email, String newPassword) {
        Assert.hasText(email, "Email cannot be blank");
        Assert.hasText(newPassword, "New password cannot be blank");

        String encodedPassword = passwordEncoder.encode(newPassword);

        organizerRepository.findByEmail(email).ifPresentOrElse(
                organizer -> organizer.changePassword(encodedPassword),
                () -> {
                    Scanner scanner = scannerRepository.findByEmail(email)
                            .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
                    scanner.changePassword(encodedPassword);
                }
        );
    }

    @Transactional(readOnly = true)
    public List<OrganizationDto> findExpiringSubscriptions(Instant expirationThreshold, Pageable pageable) {
        return organizationRepository.findBySubscriptionExpiresAtBetweenOrderBySubscriptionExpiresAtAsc(
                Instant.now(),
                expirationThreshold,
                pageable
        ).stream().map(this::toOrganizationDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrganizationDto> findRecentRegistrations(Pageable pageable) {
        return organizationRepository.findByOrderByIdDesc(pageable)
                .stream().map(this::toOrganizationDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrganizationDto> findOrganizationsByLifecycle(List<String> lifecycles, Pageable pageable) {
        return organizationRepository.findByLifecycleIn(lifecycles, pageable)
                .stream().map(this::toOrganizationDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<OrganizationDto> findAllOrganizations(Pageable pageable) {
        return organizationRepository.findAll(pageable).map(this::toOrganizationDto);
    }

    @Transactional(readOnly = true)
    public List<DailyRegistration> getDailyRegistrations(Instant startDate) {
        return organizationRepository.findDailyRegistrationsSince(startDate);
    }

    private void assertEmailIsUniqueInOrganization(String email, Long organizationId) {
        boolean emailExists = organizerRepository.existsByEmailAndOrganizationId(email, organizationId) ||
                scannerRepository.existsByEmailAndOrganizationId(email, organizationId);
        Assert.isTrue(!emailExists, "A user with this email already exists in this organization.");
    }

    private Organization getOrganizationReference(Long organizationId) {
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found."));
    }

    private OrganizerDto toOrganizerDto(Organizer organizer) {
        return new OrganizerDto(organizer.getId(), organizer.getEmail(), organizer.isEnabled(), organizer.isForcePasswordChange());
    }

    private ScannerDto toScannerDto(Scanner scanner) {
        return new ScannerDto(scanner.getId(), scanner.getEmail(), scanner.isEnabled(), scanner.isForcePasswordChange());
    }

    private OrganizationDto toOrganizationDto(Organization org) {
        return org.toDto();
    }
}
