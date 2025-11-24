package com.github.fjbaldon.attendex.platform.organization;

import com.github.fjbaldon.attendex.platform.common.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/organization/organizers")
@RequiredArgsConstructor
class OrganizerController {

    private final OrganizationFacade organizationFacade;

    @PostMapping
    public ResponseEntity<OrganizerDto> createOrganizer(
            @Valid @RequestBody CreateUserRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user) {

        OrganizerDto organizer = organizationFacade.createOrganizer(user.getOrganizationId(), request);
        return new ResponseEntity<>(organizer, HttpStatus.CREATED);
    }

    @GetMapping
    public Page<OrganizerDto> getOrganizers(
            @RequestParam(required = false) String query, // FIX: Added query
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails user) {

        return organizationFacade.findOrganizers(user.getOrganizationId(), query, pageable);
    }

    @DeleteMapping("/{organizerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrganizer(
            @PathVariable Long organizerId,
            @AuthenticationPrincipal CustomUserDetails user) {

        organizationFacade.deleteOrganizer(user.getOrganizationId(), organizerId, user.getUsername());
    }
}
