package com.github.fjbaldon.attendex.backend.service;

import com.github.fjbaldon.attendex.backend.dto.ScannerRequest;
import com.github.fjbaldon.attendex.backend.dto.ScannerResponse;
import com.github.fjbaldon.attendex.backend.mapper.ScannerMapper;
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
    private final ScannerMapper scannerMapper;

    @Transactional
    public ScannerResponse createScanner(ScannerRequest request, String organizerUsername) {
        Organizer organizer = findOrganizerByUsername(organizerUsername);
        Scanner scanner = Scanner.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .organizer(organizer)
                .build();
        Scanner savedScanner = scannerRepository.save(scanner);
        return scannerMapper.toScannerResponse(savedScanner);
    }

    @Transactional(readOnly = true)
    public List<ScannerResponse> getAllScannersByOrganizer(String organizerUsername) {
        Organizer organizer = findOrganizerByUsername(organizerUsername);
        return organizer.getScanners().stream()
                .map(scannerMapper::toScannerResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteScanner(Long scannerId, String organizerUsername) {
        Scanner scanner = scannerRepository.findById(scannerId)
                .orElseThrow(() -> new EntityNotFoundException("Scanner with ID " + scannerId + " not found"));

        if (!scanner.getOrganizer().getUsername().equals(organizerUsername)) {
            throw new AccessDeniedException("You do not have permission to delete this scanner");
        }

        scannerRepository.delete(scanner);
    }

    private Organizer findOrganizerByUsername(String username) {
        return organizerRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Organizer not found"));
    }
}
