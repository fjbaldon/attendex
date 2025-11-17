package com.github.fjbaldon.attendex.platform.organization;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
class OrganizationConfiguration {

    /**
     * This is the bean definition for the main application.
     * Spring will automatically inject the real, JPA-based implementations of the repository interfaces.
     */
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

    /**
     * This is a static factory method used ONLY for fast, isolated unit tests.
     * It manually creates and wires together the in-memory 'fake' repositories.
     * It returns a "Test Kit" containing all the necessary components for a test.
     */
    static OrganizationFacadeTestKit inMemoryTestKit() {
        var organizationRepository = new InMemoryOrganizationRepository();
        var organizerRepository = new InMemoryOrganizerRepository(organizationRepository);
        var scannerRepository = new InMemoryScannerRepository(organizationRepository);

        // The facade's external dependencies (PasswordEncoder, etc.) will be mocked by the test itself.
        var facade = new OrganizationFacade(
                organizationRepository,
                organizerRepository,
                scannerRepository,
                null, // Provided by the test
                null  // Provided by the test
        );
        return new OrganizationFacadeTestKit(facade, organizationRepository, organizerRepository, scannerRepository);
    }

    /**
     * A simple container record to pass all the necessary test components from the factory to the test class.
     */
    record OrganizationFacadeTestKit(
            OrganizationFacade facade,
            InMemoryOrganizationRepository organizationRepository,
            InMemoryOrganizerRepository organizerRepository,
            InMemoryScannerRepository scannerRepository
    ) {
    }
}
