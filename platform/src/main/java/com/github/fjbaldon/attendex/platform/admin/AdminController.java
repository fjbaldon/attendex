package com.github.fjbaldon.attendex.platform.admin;

import com.github.fjbaldon.attendex.platform.admin.dto.CreateStewardRequestDto;
import com.github.fjbaldon.attendex.platform.admin.dto.StewardDto;
import com.github.fjbaldon.attendex.platform.identity.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/stewards")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STEWARD')") // Ensure security context
class AdminController {

    private final AdminFacade adminFacade;

    @GetMapping
    public Page<StewardDto> getAllStewards(Pageable pageable) {
        return adminFacade.findAllStewards(pageable);
    }

    // ADDED: Create Steward Endpoint
    @PostMapping
    public ResponseEntity<StewardDto> createSteward(@Valid @RequestBody CreateStewardRequestDto request) {
        StewardDto newSteward = adminFacade.createSteward(request);
        return new ResponseEntity<>(newSteward, HttpStatus.CREATED);
    }

    // ADDED: Delete Steward Endpoint
    @DeleteMapping("/{stewardId}")
    public ResponseEntity<Void> deleteSteward(
            @PathVariable Long stewardId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Pass current user email to prevent self-deletion
        adminFacade.deleteSteward(stewardId, currentUser.getUsername());
        return ResponseEntity.noContent().build();
    }
}
