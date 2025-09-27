package com.github.fjbaldon.attendex.backend.controller;

import com.github.fjbaldon.attendex.backend.dto.RoleDto;
import com.github.fjbaldon.attendex.backend.dto.RoleRequest;
import com.github.fjbaldon.attendex.backend.security.CustomUserDetails;
import com.github.fjbaldon.attendex.backend.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MANAGE_ROLES')")
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<RoleDto> createRole(@Valid @RequestBody RoleRequest request, @AuthenticationPrincipal CustomUserDetails user) {
        RoleDto newRole = roleService.createRole(request, user.getOrganizationId());
        return new ResponseEntity<>(newRole, HttpStatus.CREATED);
    }

    @GetMapping("/{roleId}")
    public ResponseEntity<RoleDto> getRole(@PathVariable Long roleId, @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(roleService.getRoleById(roleId, user.getOrganizationId()));
    }

    @GetMapping
    public ResponseEntity<List<RoleDto>> getRoles(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(roleService.getRolesByOrganization(user.getOrganizationId()));
    }

    @PutMapping("/{roleId}")
    public ResponseEntity<RoleDto> updateRole(@PathVariable Long roleId, @Valid @RequestBody RoleRequest request, @AuthenticationPrincipal CustomUserDetails user) {
        RoleDto updatedRole = roleService.updateRole(roleId, request, user.getOrganizationId());
        return ResponseEntity.ok(updatedRole);
    }

    @DeleteMapping("/{roleId}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long roleId, @AuthenticationPrincipal CustomUserDetails user) {
        roleService.deleteRole(roleId, user.getOrganizationId());
        return ResponseEntity.noContent().build();
    }
}
