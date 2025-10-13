package com.github.fjbaldon.attendex.backend.controller;

import com.github.fjbaldon.attendex.backend.dto.PasswordChangeRequestDto;
import com.github.fjbaldon.attendex.backend.dto.PasswordResetRequestDto;
import com.github.fjbaldon.attendex.backend.security.CustomUserDetails;
import com.github.fjbaldon.attendex.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/me/change-password")
    @PreAuthorize("hasAuthority('FORCE_PASSWORD_CHANGE')")
    public ResponseEntity<Void> forceChangePassword(
            @Valid @RequestBody PasswordChangeRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user) {
        userService.forcePasswordChange(user.getUsername(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{userId}/reset-password")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Void> resetUserPassword(
            @PathVariable Long userId,
            @Valid @RequestBody PasswordResetRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user) {
        userService.resetUserPassword(userId, request.getNewTemporaryPassword(), user.getOrganizationId());
        return ResponseEntity.ok().build();
    }
}
