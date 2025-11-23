package com.github.fjbaldon.attendex.platform.organization;

import com.github.fjbaldon.attendex.platform.organization.dto.OrganizationDto;
import com.github.fjbaldon.attendex.platform.organization.dto.RegistrationRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
class RegistrationController {

    private final OrganizationFacade organizationFacade;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizationDto register(@Valid @RequestBody RegistrationRequestDto request) {
        return organizationFacade.registerOrganization(request);
    }
}
