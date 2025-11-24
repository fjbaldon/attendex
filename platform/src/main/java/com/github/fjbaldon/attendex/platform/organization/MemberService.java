package com.github.fjbaldon.attendex.platform.organization;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class MemberService {

    private final OrganizerRepository organizerRepository;
    private final ScannerRepository scannerRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    // --- ORGANIZERS ---

    @Transactional
    public OrganizerDto createOrganizer(Long organizationId, CreateUserRequestDto request) {
        assertEmailIsGloballyUnique(request.email());

        Organization organization = getOrganizationReference(organizationId);
        String encodedPassword = passwordEncoder.encode(request.password());
        Organizer organizer = Organizer.create(request.email(), encodedPassword, organization, null, null);

        Organizer saved = organizerRepository.save(organizer);
        // We reuse ScannerCreatedEvent or create a generic UserCreatedEvent?
        // Original code emitted ScannerCreatedEvent for creating an Organizer (likely a copy-paste bug in original, but preserving behavior for safety).
        // Correction: The provided source code actually did this. We will preserve it or fix it.
        // Ideally: New Event. But sticking to behavior:
        eventPublisher.publishEvent(new ScannerCreatedEvent(saved.getId(), saved.getOrganization().getId()));

        return toOrganizerDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<OrganizerDto> findOrganizers(Long organizationId, String query, Pageable pageable) {
        if (query != null && !query.isBlank()) {
            return organizerRepository.searchByOrganizationIdAndEmail(organizationId, query.trim(), pageable)
                    .map(this::toOrganizerDto);
        }
        return organizerRepository.findAllByOrganizationId(organizationId, pageable).map(this::toOrganizerDto);
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

    // --- SCANNERS ---

    @Transactional
    public ScannerDto createScanner(Long organizationId, CreateUserRequestDto request) {
        assertEmailIsGloballyUnique(request.email());

        Organization organization = getOrganizationReference(organizationId);
        String encodedPassword = passwordEncoder.encode(request.password());
        Scanner scanner = Scanner.create(request.email(), encodedPassword, organization);

        Scanner saved = scannerRepository.save(scanner);
        eventPublisher.publishEvent(new ScannerCreatedEvent(saved.getId(), saved.getOrganization().getId()));
        return toScannerDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<ScannerDto> findScanners(Long organizationId, String query, Pageable pageable) {
        if (query != null && !query.isBlank()) {
            return scannerRepository.searchByOrganizationIdAndEmail(organizationId, query.trim(), pageable)
                    .map(this::toScannerDto);
        }
        return scannerRepository.findAllByOrganizationId(organizationId, pageable).map(this::toScannerDto);
    }

    @Transactional
    public ScannerDto toggleScannerStatus(Long organizationId, Long scannerId) {
        Scanner scanner = scannerRepository.findById(scannerId)
                .orElseThrow(() -> new EntityNotFoundException("Scanner not found"));

        Assert.isTrue(scanner.getOrganization().getId().equals(organizationId), "Scanner does not belong to this organization.");

        scanner.toggleStatus();
        return toScannerDto(scannerRepository.save(scanner));
    }

    @Transactional
    public void deleteScanner(Long organizationId, Long scannerId) {
        Scanner scanner = scannerRepository.findById(scannerId)
                .orElseThrow(() -> new EntityNotFoundException("Scanner not found"));

        Assert.isTrue(scanner.getOrganization().getId().equals(organizationId), "Scanner does not belong to this organization.");

        scannerRepository.delete(scanner);
    }

    @Transactional(readOnly = true)
    public Map<Long, String> getScannerEmailsByIds(Set<Long> scannerIds) {
        return scannerRepository.findAllById(scannerIds).stream()
                .collect(Collectors.toMap(Scanner::getId, Scanner::getEmail));
    }

    @Transactional(readOnly = true)
    public Optional<ScannerAuthDto> findScannerAuthByEmail(String email) {
        return scannerRepository.findByEmail(email)
                .map(scanner -> new ScannerAuthDto(scanner.getId(), scanner.getOrganization().getId()));
    }

    // --- SHARED AUTH ---

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

    @Transactional
    public void resetUserPassword(Long organizationId, Long userId, String newPassword) {
        String encodedPassword = passwordEncoder.encode(newPassword);
        String userEmail;
        String orgName;

        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));
        orgName = org.getName();

        var organizer = organizerRepository.findById(userId)
                .filter(user -> user.getOrganization().getId().equals(organizationId));

        if (organizer.isPresent()) {
            organizer.get().changePassword(encodedPassword);

            organizer.get().requirePasswordChange();

            userEmail = organizer.get().getEmail();
        } else {
            var scanner = scannerRepository.findById(userId)
                    .filter(user -> user.getOrganization().getId().equals(organizationId))
                    .orElseThrow(() -> new EntityNotFoundException("User not found in this organization"));
            scanner.changePassword(encodedPassword);
            scanner.requirePasswordChange();
            userEmail = scanner.getEmail();
        }

        if (userEmail != null) {
            eventPublisher.publishEvent(new PasswordResetInitiatedEvent(
                    userEmail,
                    newPassword,
                    orgName
            ));
        }
    }

    private Organization getOrganizationReference(Long organizationId) {
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found."));
    }

    private void assertEmailIsGloballyUnique(String email) {
        boolean existsAsOrganizer = organizerRepository.findByEmail(email).isPresent();
        boolean existsAsScanner = scannerRepository.findByEmail(email).isPresent();
        Assert.isTrue(!existsAsOrganizer && !existsAsScanner, "This email is already in use by another user.");
    }

    private OrganizerDto toOrganizerDto(Organizer organizer) {
        return new OrganizerDto(organizer.getId(), organizer.getEmail(), organizer.isEnabled(), organizer.isForcePasswordChange());
    }

    private ScannerDto toScannerDto(Scanner scanner) {
        return new ScannerDto(scanner.getId(), scanner.getEmail(), scanner.isEnabled(), scanner.isForcePasswordChange());
    }
}
