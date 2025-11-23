package com.github.fjbaldon.attendex.platform.identity;

import com.github.fjbaldon.attendex.platform.identity.dto.PasswordChangeRequestDto;
import com.github.fjbaldon.attendex.platform.organization.OrganizationFacade;
import com.github.fjbaldon.attendex.platform.organization.dto.PasswordResetRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
class UserController {

    private final IdentityFacade identityFacade;
    private final OrganizationFacade organizationFacade;

    @PostMapping("/me/change-password")
    @PreAuthorize("hasAuthority('FORCE_PASSWORD_CHANGE')")
    public ResponseEntity<Void> forceChangePassword(
            @Valid @RequestBody PasswordChangeRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user) {

        identityFacade.forceChangePassword(user, request);
        return ResponseEntity.ok().build();
    }

    // NEW: Voluntary Password Change
    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateMyPassword(
            @Valid @RequestBody PasswordChangeRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user) {

        // Re-use the logic, as it handles both Organizers and Stewards
        identityFacade.forceChangePassword(user, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{userId}/reset-password")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Void> resetUserPassword(
            @PathVariable Long userId,
            @Valid @RequestBody PasswordResetRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user) {
        organizationFacade.resetUserPassword(user.getOrganizationId(), userId, request.newPassword());
        return ResponseEntity.ok().build();
    }
}
