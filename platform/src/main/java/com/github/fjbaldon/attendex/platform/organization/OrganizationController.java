package com.github.fjbaldon.attendex.platform.organization;

import com.github.fjbaldon.attendex.platform.organization.dto.OrganizationDto;
import com.github.fjbaldon.attendex.platform.organization.dto.RegistrationRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
class OrganizationController {

    private final OrganizationFacade organizationFacade;

    @PostMapping
    public OrganizationDto register(@Valid @RequestBody RegistrationRequestDto request) {
        return organizationFacade.registerOrganization(request);
    }
}
