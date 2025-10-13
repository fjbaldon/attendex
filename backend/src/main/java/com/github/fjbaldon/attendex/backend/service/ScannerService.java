package com.github.fjbaldon.attendex.backend.service;

import com.github.fjbaldon.attendex.backend.dto.ScannerResponse;
import com.github.fjbaldon.attendex.backend.dto.UserCreateRequestDto;
import com.github.fjbaldon.attendex.backend.exception.EmailAlreadyExistsException;
import com.github.fjbaldon.attendex.backend.model.Organization;
import com.github.fjbaldon.attendex.backend.model.Scanner;
import com.github.fjbaldon.attendex.backend.repository.OrganizerRepository;
import com.github.fjbaldon.attendex.backend.repository.ScannerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScannerService {

    private final ScannerRepository scannerRepository;
    private final OrganizerRepository organizerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ScannerResponse createScanner(UserCreateRequestDto request, Long organizationId) {
        if (organizerRepository.existsByEmailAndOrganizationId(request.getEmail(), organizationId) ||
                scannerRepository.existsByEmailAndOrganizationId(request.getEmail(), organizationId)) {
            throw new EmailAlreadyExistsException("Email '" + request.getEmail() + "' is already in use for this organization");
        }

        Organization orgReference = new Organization();
        orgReference.setId(organizationId);

        Scanner scanner = Scanner.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getTemporaryPassword()))
                .organization(orgReference)
                .build();

        Scanner savedScanner = scannerRepository.save(scanner);
        return toScannerResponse(savedScanner);
    }

    @Transactional(readOnly = true)
    public List<ScannerResponse> getAllScannersByOrganization(Long organizationId) {
        return scannerRepository.findAllByOrganizationId(organizationId).stream()
                .map(this::toScannerResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteScanner(Long scannerId, Long organizationId) {
        Scanner scanner = scannerRepository.findById(scannerId)
                .orElseThrow(() -> new EntityNotFoundException("Scanner with ID " + scannerId + " not found"));

        if (!scanner.getOrganization().getId().equals(organizationId)) {
            throw new EntityNotFoundException("Scanner with ID " + scannerId + " not found in your organization");
        }

        scannerRepository.delete(scanner);
    }

    private ScannerResponse toScannerResponse(Scanner scanner) {
        return ScannerResponse.builder()
                .id(scanner.getId())
                .email(scanner.getEmail())
                .build();
    }
}
