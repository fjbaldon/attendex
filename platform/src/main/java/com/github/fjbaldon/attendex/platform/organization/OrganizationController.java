package com.github.fjbaldon.attendex.platform.organization;

import com.github.fjbaldon.attendex.platform.identity.CustomUserDetails;
import com.github.fjbaldon.attendex.platform.organization.dto.OrganizationDto;
import com.github.fjbaldon.attendex.platform.organization.dto.RegistrationRequestDto;
import com.github.fjbaldon.attendex.platform.organization.dto.UpdateOrganizationDetailsDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/organization")
@RequiredArgsConstructor
class OrganizationController {

    private final OrganizationFacade organizationFacade;

    @PostMapping
    public OrganizationDto register(@Valid @RequestBody RegistrationRequestDto request) {
        return organizationFacade.registerOrganization(request);
    }

    @GetMapping
    public OrganizationDto getOrganization(@AuthenticationPrincipal CustomUserDetails user) {
        return organizationFacade.findOrganizationById(user.getOrganizationId());
    }

    @PutMapping
    public OrganizationDto updateOrganization(
            @Valid @RequestBody UpdateOrganizationDetailsDto request,
            @AuthenticationPrincipal CustomUserDetails user) {
        return organizationFacade.updateOrganizationDetails(user.getOrganizationId(), request);
    }
}
