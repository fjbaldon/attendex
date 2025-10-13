package com.github.fjbaldon.attendex.backend.controller;

import com.github.fjbaldon.attendex.backend.dto.OrganizerResponseDto;
import com.github.fjbaldon.attendex.backend.dto.PaginatedResponseDto;
import com.github.fjbaldon.attendex.backend.dto.UserCreateRequestDto;
import com.github.fjbaldon.attendex.backend.security.CustomUserDetails;
import com.github.fjbaldon.attendex.backend.service.OrganizerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/organizers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANIZER')")
public class OrganizerController {

    private final OrganizerService organizerService;

    @PostMapping
    public ResponseEntity<OrganizerResponseDto> createOrganizer(
            @Valid @RequestBody UserCreateRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user) {
        OrganizerResponseDto newOrganizer = organizerService.createOrganizer(request, user.getOrganizationId());
        return new ResponseEntity<>(newOrganizer, HttpStatus.CREATED);
    }

    @GetMapping("/{organizerId}")
    public ResponseEntity<OrganizerResponseDto> getOrganizer(@PathVariable Long organizerId, @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(organizerService.getOrganizerById(organizerId, user.getOrganizationId()));
    }

    @GetMapping
    public ResponseEntity<PaginatedResponseDto<OrganizerResponseDto>> getOrganizers(
            @AuthenticationPrincipal CustomUserDetails user,
            Pageable pageable) {
        return ResponseEntity.ok(organizerService.getOrganizersByOrganization(user.getOrganizationId(), pageable));
    }

    @DeleteMapping("/{organizerId}")
    public ResponseEntity<Void> removeOrganizer(
            @PathVariable Long organizerId,
            @AuthenticationPrincipal CustomUserDetails user) {
        organizerService.removeOrganizer(organizerId, user.getOrganizationId(), user.getUsername());
        return ResponseEntity.noContent().build();
    }
}
