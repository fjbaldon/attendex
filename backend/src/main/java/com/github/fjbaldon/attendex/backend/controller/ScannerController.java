package com.github.fjbaldon.attendex.backend.controller;

import com.github.fjbaldon.attendex.backend.dto.ScannerRequest;
import com.github.fjbaldon.attendex.backend.dto.ScannerResponse;
import com.github.fjbaldon.attendex.backend.service.ScannerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/scanners")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANIZER')")
public class ScannerController {

    private final ScannerService scannerService;

    @PostMapping
    public ResponseEntity<ScannerResponse> createScanner(@Valid @RequestBody ScannerRequest request, Principal principal) {
        ScannerResponse createdScanner = scannerService.createScanner(request, principal.getName());
        return new ResponseEntity<>(createdScanner, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ScannerResponse>> getOrganizerScanners(Principal principal) {
        return ResponseEntity.ok(scannerService.getAllScannersByOrganizer(principal.getName()));
    }

    @DeleteMapping("/{scannerId}")
    public ResponseEntity<Void> deleteScanner(@PathVariable Long scannerId, Principal principal) {
        scannerService.deleteScanner(scannerId, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
