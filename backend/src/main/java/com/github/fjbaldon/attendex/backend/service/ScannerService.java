package com.github.fjbaldon.attendex.backend.service;

import com.github.fjbaldon.attendex.backend.dto.ScannerRequest;
import com.github.fjbaldon.attendex.backend.dto.ScannerResponse;
import com.github.fjbaldon.attendex.backend.model.Organizer;
import com.github.fjbaldon.attendex.backend.model.Scanner;
import com.github.fjbaldon.attendex.backend.repository.OrganizerRepository;
import com.github.fjbaldon.attendex.backend.repository.ScannerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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
    public ScannerResponse createScanner(ScannerRequest request, String organizerEmail) {
        Organizer organizer = findOrganizerByEmail(organizerEmail);
        Scanner scanner = Scanner.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .organizer(organizer)
                .build();
        Scanner savedScanner = scannerRepository.save(scanner);
        return toScannerResponse(savedScanner);
    }

    @Transactional(readOnly = true)
    public List<ScannerResponse> getAllScannersByOrganizer(String organizerEmail) {
        Organizer organizer = findOrganizerByEmail(organizerEmail);
        return organizer.getScanners().stream()
                .map(this::toScannerResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteScanner(Long scannerId, String organizerEmail) {
        Scanner scanner = scannerRepository.findById(scannerId)
                .orElseThrow(() -> new EntityNotFoundException("Scanner with ID " + scannerId + " not found"));

        if (!scanner.getOrganizer().getEmail().equals(organizerEmail)) {
            throw new AccessDeniedException("You do not have permission to delete this scanner");
        }

        scannerRepository.delete(scanner);
    }

    private Organizer findOrganizerByEmail(String email) {
        return organizerRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Organizer not found"));
    }

    private ScannerResponse toScannerResponse(Scanner scanner) {
        return ScannerResponse.builder()
                .id(scanner.getId())
                .email(scanner.getEmail())
                .build();
    }
}
