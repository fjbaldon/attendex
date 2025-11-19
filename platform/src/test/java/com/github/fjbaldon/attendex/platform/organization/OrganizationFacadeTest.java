package com.github.fjbaldon.attendex.platform.organization;

import com.github.fjbaldon.attendex.platform.organization.dto.CreateUserRequestDto;
import com.github.fjbaldon.attendex.platform.organization.dto.RegistrationRequestDto;
import com.github.fjbaldon.attendex.platform.organization.events.OrganizationRegisteredEvent;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationFacadeTest {

    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private OrganizerRepository organizerRepository;
    @Mock
    private ScannerRepository scannerRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrganizationFacade organizationFacade;

    @Test
    void registerOrganization_shouldSucceed_whenDataIsValid() {
        // Given
        var request = new RegistrationRequestDto("Test Corp", "admin@test.com", "password");

        when(organizerRepository.existsByOrganizationName("Test Corp")).thenReturn(false);
        when(organizerRepository.findByEmail("admin@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encoded_password");

        // Mock saving the organization to return an entity with an ID
        when(organizationRepository.save(any(Organization.class))).thenAnswer(invocation -> {
            Organization org = invocation.getArgument(0);
            // Reflection to simulate DB ID assignment (optional but good for completeness)
            var idField = Organization.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(org, 1L);
            return org;
        });

        // When
        organizationFacade.registerOrganization(request);

        // Then
        verify(organizationRepository).save(any(Organization.class));
        verify(organizerRepository).save(any(Organizer.class));

        // Verify event publication
        ArgumentCaptor<OrganizationRegisteredEvent> eventCaptor = ArgumentCaptor.forClass(OrganizationRegisteredEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().organizationName()).isEqualTo("Test Corp");
        assertThat(eventCaptor.getValue().organizerEmail()).isEqualTo("admin@test.com");
    }

    @Test
    void registerOrganization_shouldFail_whenNameExists() {
        // Given
        var request = new RegistrationRequestDto("Existing Corp", "user@corp.com", "password");
        when(organizerRepository.existsByOrganizationName("Existing Corp")).thenReturn(true);

        // When
        Throwable thrown = catchThrowable(() -> organizationFacade.registerOrganization(request));

        // Then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Organization name is already taken");
        verify(organizationRepository, never()).save(any());
    }

    @Test
    void createOrganizer_shouldFail_whenEmailIsInUse() {
        // Given
        Long orgId = 1L;
        var request = new CreateUserRequestDto("existing@test.com", "password");

        // Simulate email collision
        when(organizerRepository.existsByEmailAndOrganizationId("existing@test.com", orgId)).thenReturn(true);

        // When
        Throwable thrown = catchThrowable(() -> organizationFacade.createOrganizer(orgId, request));

        // Then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A user with this email already exists in this organization.");
    }

    @Test
    void createOrganizer_shouldFail_whenOrganizationNotFound() {
        // Given
        Long orgId = 99L;
        var request = new CreateUserRequestDto("new@test.com", "password");

        when(organizerRepository.existsByEmailAndOrganizationId(any(), any())).thenReturn(false);
        when(scannerRepository.existsByEmailAndOrganizationId(any(), any())).thenReturn(false);
        when(organizationRepository.findById(orgId)).thenReturn(Optional.empty());

        // When
        Throwable thrown = catchThrowable(() -> organizationFacade.createOrganizer(orgId, request));

        // Then
        assertThat(thrown)
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Organization not found.");
    }
}
