package com.github.fjbaldon.attendex.backend.controller;

import com.github.fjbaldon.attendex.backend.dto.ScannerResponse;
import com.github.fjbaldon.attendex.backend.dto.UserCreateRequestDto;
import com.github.fjbaldon.attendex.backend.security.CustomUserDetails;
import com.github.fjbaldon.attendex.backend.service.ScannerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/scanners")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANIZER')")
public class ScannerController {

    private final ScannerService scannerService;

    @PostMapping
    public ResponseEntity<ScannerResponse> createScanner(@Valid @RequestBody UserCreateRequestDto request, @AuthenticationPrincipal CustomUserDetails user) {
        ScannerResponse createdScanner = scannerService.createScanner(request, user.getOrganizationId());
        return new ResponseEntity<>(createdScanner, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ScannerResponse>> getOrganizationScanners(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(scannerService.getAllScannersByOrganization(user.getOrganizationId()));
    }

    @DeleteMapping("/{scannerId}")
    public ResponseEntity<Void> deleteScanner(@PathVariable Long scannerId, @AuthenticationPrincipal CustomUserDetails user) {
        scannerService.deleteScanner(scannerId, user.getOrganizationId());
        return ResponseEntity.noContent().build();
    }
}
