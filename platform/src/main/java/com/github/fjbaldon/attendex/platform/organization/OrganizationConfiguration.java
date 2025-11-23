package com.github.fjbaldon.attendex.platform.organization;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
class OrganizationConfiguration {

    @Bean
    OrganizationFacade organizationFacade(
            OrganizationRepository organizationRepository,
            OrganizerRepository organizerRepository,
            ScannerRepository scannerRepository,
            PasswordEncoder passwordEncoder,
            ApplicationEventPublisher eventPublisher
    ) {
        return new OrganizationFacade(
                organizationRepository,
                organizerRepository,
                scannerRepository,
                passwordEncoder,
                eventPublisher
        );
    }
}
