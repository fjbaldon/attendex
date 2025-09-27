package com.github.fjbaldon.attendex.backend.service;

import com.github.fjbaldon.attendex.backend.dto.UserCreateRequestDto;
import com.github.fjbaldon.attendex.backend.model.*;
import com.github.fjbaldon.attendex.backend.repository.OrganizerRepository;
import com.github.fjbaldon.attendex.backend.repository.RoleRepository;
import com.github.fjbaldon.attendex.backend.repository.ScannerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final OrganizerRepository organizerRepository;
    private final ScannerRepository scannerRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void createUser(UserCreateRequestDto request, Long organizationId) {
        if (organizerRepository.existsByEmailAndOrganizationId(request.getEmail(), organizationId) ||
                scannerRepository.existsByEmailAndOrganizationId(request.getEmail(), organizationId)) {
            throw new IllegalStateException("A user with this email already exists in the organization.");
        }

        Role assignedRole = roleRepository.findByIdAndOrganizationId(request.getRoleId(), organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found in your organization."));

        Organization orgReference = new Organization();
        orgReference.setId(organizationId);

        boolean isScannerRole = assignedRole.getPermissions().contains(Permission.SCAN_ATTENDANCE) &&
                assignedRole.getPermissions().size() == 1;

        if (isScannerRole) {
            Scanner newUser = Scanner.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getTemporaryPassword()))
                    .organization(orgReference)
                    .role(assignedRole)
                    .enabled(true)
                    .forcePasswordChange(true)
                    .build();
            scannerRepository.save(newUser);
        } else {
            Organizer newUser = Organizer.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getTemporaryPassword()))
                    .organization(orgReference)
                    .role(assignedRole)
                    .enabled(true)
                    .forcePasswordChange(true)
                    .build();
            organizerRepository.save(newUser);
        }
    }

    @Transactional
    public void forcePasswordChange(String userEmail, String newPassword) {
        organizerRepository.findByEmail(userEmail).ifPresentOrElse(
                organizer -> {
                    organizer.setPassword(passwordEncoder.encode(newPassword));
                    organizer.setForcePasswordChange(false);
                    organizerRepository.save(organizer);
                },
                () -> {
                    Scanner scanner = scannerRepository.findByEmail(userEmail)
                            .orElseThrow(() -> new EntityNotFoundException("User not found."));
                    scanner.setPassword(passwordEncoder.encode(newPassword));
                    scanner.setForcePasswordChange(false);
                    scannerRepository.save(scanner);
                }
        );
    }

    @Transactional
    public void resetUserPassword(Long userId, String newTemporaryPassword, Long organizationId) {
        Optional<Organizer> organizerOpt = organizerRepository.findByIdAndOrganizationId(userId, organizationId);

        if (organizerOpt.isPresent()) {
            Organizer organizer = organizerOpt.get();
            organizer.setPassword(passwordEncoder.encode(newTemporaryPassword));
            organizer.setForcePasswordChange(true);
            organizerRepository.save(organizer);
            return;
        }

        Optional<Scanner> scannerOpt = scannerRepository.findByIdAndOrganizationId(userId, organizationId);

        if (scannerOpt.isPresent()) {
            Scanner scanner = scannerOpt.get();
            scanner.setPassword(passwordEncoder.encode(newTemporaryPassword));
            scanner.setForcePasswordChange(true);
            scannerRepository.save(scanner);
            return;
        }

        throw new EntityNotFoundException("User with ID " + userId + " not found in your organization.");
    }
}
