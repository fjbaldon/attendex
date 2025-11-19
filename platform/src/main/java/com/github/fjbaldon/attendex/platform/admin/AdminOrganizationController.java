package com.github.fjbaldon.attendex.platform.admin;

import com.github.fjbaldon.attendex.platform.admin.dto.UpdateOrganizationLifecycleDto;
import com.github.fjbaldon.attendex.platform.admin.dto.UpdateSubscriptionDto;
import com.github.fjbaldon.attendex.platform.organization.OrganizationFacade;
import com.github.fjbaldon.attendex.platform.organization.dto.OrganizationDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/organizations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STEWARD')")
class AdminOrganizationController {

    private final OrganizationFacade organizationFacade;
    private final AdminFacade adminFacade;

    @GetMapping
    public Page<OrganizationDto> getAllOrganizations(Pageable pageable) {
        return organizationFacade.findAllOrganizations(pageable);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrganizationDto> updateLifecycle(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrganizationLifecycleDto dto) {
        return ResponseEntity.ok(adminFacade.updateOrganizationLifecycle(id, dto));
    }

    @PutMapping("/{id}/subscription")
    public ResponseEntity<OrganizationDto> updateSubscription(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSubscriptionDto dto) {
        return ResponseEntity.ok(adminFacade.updateOrganizationSubscription(id, dto));
    }
}
