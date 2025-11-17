package com.github.fjbaldon.attendex.platform.organization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fjbaldon.attendex.platform.organization.dto.RegistrationRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class RegistrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizerRepository organizerRepository;

    @Test
    void registerOrganization_whenDataIsValid_shouldSucceedAndPersistEntities() throws Exception {
        // Given
        var request = new RegistrationRequestDto(
                "Test Corp",
                "organizer@testcorp.com",
                "ValidPassword123"
        );

        // When
        mockMvc.perform(post("/api/v1/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // Then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Corp"))
                .andExpect(jsonPath("$.lifecycle").value("INACTIVE"));

        // And
        assertThat(organizationRepository.count()).isEqualTo(1);
        Organizer savedOrganizer = organizerRepository.findByEmail("organizer@testcorp.com").orElseThrow();
        assertThat(savedOrganizer.isEnabled()).isFalse();
        assertThat(savedOrganizer.getVerificationToken()).isNotNull();
    }
}
