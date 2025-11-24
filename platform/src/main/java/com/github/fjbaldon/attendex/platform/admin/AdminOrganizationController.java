package com.github.fjbaldon.attendex.platform.admin;

import com.github.fjbaldon.attendex.platform.organization.OrganizationFacade;
import com.github.fjbaldon.attendex.platform.organization.OrganizationDto;
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
    public Page<OrganizationDto> getAllOrganizations(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String lifecycle,
            @RequestParam(required = false) String subscriptionType,
            Pageable pageable
    ) {
        return organizationFacade.findAllOrganizations(query, lifecycle, subscriptionType, pageable);
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
