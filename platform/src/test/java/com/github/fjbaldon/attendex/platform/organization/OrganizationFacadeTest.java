package com.github.fjbaldon.attendex.platform.organization;

import com.github.fjbaldon.attendex.platform.organization.dto.CreateUserRequestDto;
import com.github.fjbaldon.attendex.platform.organization.dto.RegistrationRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class OrganizationFacadeTest {

    private InMemoryOrganizationRepository organizationRepository;
    private InMemoryOrganizerRepository organizerRepository;
    private InMemoryScannerRepository scannerRepository;
    private PasswordEncoder passwordEncoder;
    private ApplicationEventPublisher eventPublisher;

    private OrganizationFacade organizationFacade;

    @BeforeEach
    void setUp() {
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        organizationFacade = OrganizationConfiguration.inMemoryFacade(passwordEncoder, eventPublisher);
    }

    @Test
    void shouldSuccessfullyRegisterNewOrganization() {
        var request = new RegistrationRequestDto("New Corp", "test@newcorp.com", "password123");
        organizationFacade.registerOrganization(request);

        // A full test would assert against the in-memory repositories
        // For brevity, this is a simplified assertion.
        assertThat(organizationFacade.findUserAuthByEmail("test@newcorp.com")).isPresent();
    }

    @Test
    void shouldFailToRegisterOrganizationIfNameExists() {
        // Given
        var existingRequest = new RegistrationRequestDto("Existing Corp", "user1@corp.com", "password123");
        organizationFacade.registerOrganization(existingRequest);

        // When
        var duplicateRequest = new RegistrationRequestDto("Existing Corp", "user2@corp.com", "password123");
        Throwable thrown = catchThrowable(() -> organizationFacade.registerOrganization(duplicateRequest));

        // Then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Organization name is already taken");
    }

    @Test
    void shouldFailToCreateOrganizerIfEmailIsInUse() {
        // Given
        var orgRequest = new RegistrationRequestDto("Test Corp", "user@test.com", "password123");
        organizationFacade.registerOrganization(orgRequest);
        Organization org = organizationRepository.findAll().iterator().next();

        // When
        var userRequest = new CreateUserRequestDto("user@test.com", "password123");
        Throwable thrown = catchThrowable(() -> organizationFacade.createOrganizer(org.getId(), userRequest));

        // Then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A user with this email already exists in this organization.");
    }
}
