package com.github.fjbaldon.attendex.backend.controller;

import com.github.fjbaldon.attendex.backend.dto.OrganizationResponseDto;
import com.github.fjbaldon.attendex.backend.dto.OrganizationUpdateRequest;
import com.github.fjbaldon.attendex.backend.security.CustomUserDetails;
import com.github.fjbaldon.attendex.backend.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/organization")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANIZER')")
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping
    public ResponseEntity<OrganizationResponseDto> getOrganization(@AuthenticationPrincipal CustomUserDetails user) {
        OrganizationResponseDto organizationDto = organizationService.getOrganizationDtoById(user.getOrganizationId());
        return ResponseEntity.ok(organizationDto);
    }

    @PutMapping
    public ResponseEntity<OrganizationResponseDto> updateOrganization(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody OrganizationUpdateRequest request) {
        OrganizationResponseDto updatedOrganizationDto = organizationService.updateOrganization(user.getOrganizationId(), request);
        return ResponseEntity.ok(updatedOrganizationDto);
    }
}
