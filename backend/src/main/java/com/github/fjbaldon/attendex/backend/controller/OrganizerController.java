package com.github.fjbaldon.attendex.backend.controller;

import com.github.fjbaldon.attendex.backend.dto.OrganizerResponseDto;
import com.github.fjbaldon.attendex.backend.dto.OrganizerRoleUpdateRequestDto;
import com.github.fjbaldon.attendex.backend.security.CustomUserDetails;
import com.github.fjbaldon.attendex.backend.service.OrganizerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organizers")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MANAGE_USERS')")
public class OrganizerController {

    private final OrganizerService organizerService;

    @GetMapping("/{organizerId}")
    public ResponseEntity<OrganizerResponseDto> getOrganizer(@PathVariable Long organizerId, @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(organizerService.getOrganizerById(organizerId, user.getOrganizationId()));
    }

    @GetMapping
    public ResponseEntity<Page<OrganizerResponseDto>> getOrganizers(
            @AuthenticationPrincipal CustomUserDetails user,
            Pageable pageable) {
        return ResponseEntity.ok(organizerService.getOrganizersByOrganization(user.getOrganizationId(), pageable));
    }

    @PutMapping("/{organizerId}/role")
    public ResponseEntity<OrganizerResponseDto> updateOrganizerRole(
            @PathVariable Long organizerId,
            @Valid @RequestBody OrganizerRoleUpdateRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user) {
        OrganizerResponseDto updatedOrganizer = organizerService.updateOrganizerRole(organizerId, request.getRoleId(), user.getOrganizationId());
        return ResponseEntity.ok(updatedOrganizer);
    }

    @DeleteMapping("/{organizerId}")
    public ResponseEntity<Void> removeOrganizer(
            @PathVariable Long organizerId,
            @AuthenticationPrincipal CustomUserDetails user) {
        organizerService.removeOrganizer(organizerId, user.getOrganizationId(), user.getUsername());
        return ResponseEntity.noContent().build();
    }
}
