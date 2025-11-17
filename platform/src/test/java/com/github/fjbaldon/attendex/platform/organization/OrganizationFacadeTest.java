package com.github.fjbaldon.attendex.platform.organization;

import com.github.fjbaldon.attendex.platform.organization.OrganizationConfiguration.OrganizationFacadeTestKit;
import com.github.fjbaldon.attendex.platform.organization.dto.CreateUserRequestDto;
import com.github.fjbaldon.attendex.platform.organization.dto.RegistrationRequestDto;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class OrganizationFacadeTest {

    private OrganizationFacade organizationFacade;
    private InMemoryOrganizationRepository organizationRepository;
    private InMemoryOrganizerRepository organizerRepository;

    @BeforeEach
    void setUp() {
        // Get the fully wired-up test kit
        OrganizationFacadeTestKit testKit = OrganizationConfiguration.inMemoryTestKit();
        organizationFacade = testKit.facade();
        organizationRepository = testKit.organizationRepository();
        organizerRepository = testKit.organizerRepository();
    }

    @Test
    void shouldFailToRegisterOrganizationIfNameExists() {
        // Given: An organization named "Existing Corp" is already saved in the fake DB.
        organizationRepository.save(Organization.register("Existing Corp"));
        assertThat(organizationRepository.count()).isEqualTo(1); // Verify setup

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
        // Given: An organization and an organizer are saved in the fake DB.
        var org = organizationRepository.save(Organization.register("Test Corp"));
        var existingUser = Organizer.create("user@test.com", "password", org, null, null);
        organizerRepository.save(existingUser);

        // When
        var userRequest = new CreateUserRequestDto("user@test.com", "password123");
        Throwable thrown = catchThrowable(() -> organizationFacade.createOrganizer(org.getId(), userRequest));

        // Then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A user with this email already exists in this organization.");
    }

    @Test
    void shouldFailToCreateOrganizerForNonExistentOrganization() {
        // When
        var userRequest = new CreateUserRequestDto("user@test.com", "password123");
        Throwable thrown = catchThrowable(() -> organizationFacade.createOrganizer(999L, userRequest));

        // Then
        assertThat(thrown)
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Organization not found.");
    }
}
