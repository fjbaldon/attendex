package com.github.fjbaldon.attendex.backend.service;

import com.github.fjbaldon.attendex.backend.dto.RegisterRequest;
import com.github.fjbaldon.attendex.backend.exception.EmailAlreadyExistsException;
import com.github.fjbaldon.attendex.backend.model.Organizer;
import com.github.fjbaldon.attendex.backend.model.Organization;
import com.github.fjbaldon.attendex.backend.repository.OrganizerRepository;
import com.github.fjbaldon.attendex.backend.repository.OrganizationRepository;
import com.github.fjbaldon.attendex.backend.repository.ScannerRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final OrganizerRepository organizerRepository;
    private final ScannerRepository scannerRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${jwt.verification-token-expiration-hours:24}")
    private long tokenExpirationHours;

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

        String token = UUID.randomUUID().toString();

        Organizer organizer = Organizer.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .organization(savedOrganization)
                .enabled(false)
                .forcePasswordChange(false)
                .verificationToken(token)
                .tokenExpiryDate(Instant.now().plus(tokenExpirationHours, ChronoUnit.HOURS))
                .build();
        organizerRepository.save(organizer);

        try {
            emailService.sendVerificationEmail(request.getEmail(), request.getOrganizationName(), token);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email.", e);
        }
    }

    @Transactional
    public void verifyUser(String token) {
        Organizer organizer = organizerRepository.findByVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token."));

        if (organizer.isEnabled()) {
            throw new IllegalStateException("This account has already been verified. Please log in.");
        }

        if (organizer.getTokenExpiryDate().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Verification token has expired.");
        }

        organizer.setEnabled(true);
        organizer.setVerificationToken(null);
        organizer.setTokenExpiryDate(null);
        organizerRepository.save(organizer);
    }
}
