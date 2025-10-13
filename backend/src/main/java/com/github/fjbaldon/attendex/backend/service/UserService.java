package com.github.fjbaldon.attendex.backend.service;

import com.github.fjbaldon.attendex.backend.model.Organizer;
import com.github.fjbaldon.attendex.backend.model.Scanner;
import com.github.fjbaldon.attendex.backend.repository.OrganizerRepository;
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
    private final PasswordEncoder passwordEncoder;

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
