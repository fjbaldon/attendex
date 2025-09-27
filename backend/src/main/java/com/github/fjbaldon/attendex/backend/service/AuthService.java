package com.github.fjbaldon.attendex.backend.service;

import com.github.fjbaldon.attendex.backend.dto.RegisterRequest;
import com.github.fjbaldon.attendex.backend.exception.EmailAlreadyExistsException;
import com.github.fjbaldon.attendex.backend.model.Organizer;
import com.github.fjbaldon.attendex.backend.model.Organization;
import com.github.fjbaldon.attendex.backend.model.Permission;
import com.github.fjbaldon.attendex.backend.model.Role;
import com.github.fjbaldon.attendex.backend.repository.OrganizerRepository;
import com.github.fjbaldon.attendex.backend.repository.OrganizationRepository;
import com.github.fjbaldon.attendex.backend.repository.RoleRepository;
import com.github.fjbaldon.attendex.backend.repository.ScannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final OrganizerRepository organizerRepository;
    private final ScannerRepository scannerRepository;
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void registerNewOrganization(RegisterRequest request) {
        if (organizerRepository.findByEmail(request.getEmail()).isPresent() ||
                scannerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email '" + request.getEmail() + "' is already taken");
        }

        if (organizationRepository.existsByName(request.getOrganizationName())) {
            throw new IllegalArgumentException("Organization with name '" + request.getOrganizationName() + "' already exists");
        }

        Organization organization = Organization.builder()
                .name(request.getOrganizationName())
                .build();
        Organization savedOrganization = organizationRepository.save(organization);

        Role adminRole = Role.builder()
                .name("Admin")
                .organization(savedOrganization)
                .permissions(Set.of(Permission.values())) // Grant all permissions
                .build();
        roleRepository.save(adminRole);

        Role scannerRole = Role.builder()
                .name("Scanner")
                .organization(savedOrganization)
                .permissions(Set.of(Permission.SCAN_ATTENDANCE))
                .build();
        roleRepository.save(scannerRole);

        Organizer organizer = Organizer.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .organization(savedOrganization)
                .role(adminRole)
                .enabled(true)
                .forcePasswordChange(false)
                .build();

        organizerRepository.save(organizer);
    }
}
