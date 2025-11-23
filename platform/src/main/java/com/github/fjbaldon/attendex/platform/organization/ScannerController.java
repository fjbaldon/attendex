package com.github.fjbaldon.attendex.platform.organization;

import com.github.fjbaldon.attendex.platform.identity.CustomUserDetails;
import com.github.fjbaldon.attendex.platform.organization.dto.CreateUserRequestDto;
import com.github.fjbaldon.attendex.platform.organization.dto.ScannerDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/organization/scanners")
@RequiredArgsConstructor
class ScannerController {

    private final OrganizationFacade organizationFacade;

    @PostMapping
    public ResponseEntity<ScannerDto> createScanner(
            @Valid @RequestBody CreateUserRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user) {

        ScannerDto scanner = organizationFacade.createScanner(user.getOrganizationId(), request);
        return new ResponseEntity<>(scanner, HttpStatus.CREATED);
    }

    @GetMapping
    public Page<ScannerDto> getScanners(
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails user) {

        return organizationFacade.findScanners(user.getOrganizationId(), pageable);
    }

    @PatchMapping("/{scannerId}/status")
    public ResponseEntity<ScannerDto> toggleScannerStatus(
            @PathVariable Long scannerId,
            @AuthenticationPrincipal CustomUserDetails user) {

        ScannerDto updatedScanner = organizationFacade.toggleScannerStatus(user.getOrganizationId(), scannerId);
        return ResponseEntity.ok(updatedScanner);
    }

    @DeleteMapping("/{scannerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteScanner(
            @PathVariable Long scannerId,
            @AuthenticationPrincipal CustomUserDetails user) {

        organizationFacade.deleteScanner(user.getOrganizationId(), scannerId);
    }
}
