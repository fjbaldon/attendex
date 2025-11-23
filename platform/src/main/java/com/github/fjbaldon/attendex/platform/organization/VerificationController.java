package com.github.fjbaldon.attendex.platform.organization;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organizations/verify")
@RequiredArgsConstructor
class VerificationController {

    private final OrganizationFacade organizationFacade;

    @GetMapping
    public ResponseEntity<String> verify(@RequestParam("token") String token) {
        organizationFacade.verifyOrganizer(token);
        return ResponseEntity.ok("Account verified successfully. You may now log in.");
    }
}
