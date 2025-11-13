package com.github.fjbaldon.attendex.platform.organization;

import com.github.fjbaldon.attendex.platform.organization.dto.OrganizationDto;
import com.github.fjbaldon.attendex.platform.organization.dto.RegistrationRequestDto;
import com.github.fjbaldon.attendex.platform.organization.dto.UserAuthDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrganizationFacade {

    private final OrganizationRepository organizationRepository;
    private final OrganizerRepository organizerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public OrganizationDto registerOrganization(RegistrationRequestDto request) {
        Assert.isTrue(!organizerRepository.existsByOrganizationName(request.organizationName()), "Organization name is already taken");
        Assert.isTrue(organizerRepository.findByEmail(request.email()).isEmpty(), "Email is already in use");

        Organization organization = Organization.register(request.organizationName());
        Organization savedOrganization = organizationRepository.save(organization);

        String encodedPassword = passwordEncoder.encode(request.password());
        Organizer organizer = Organizer.create(request.email(), encodedPassword, savedOrganization);
        organizerRepository.save(organizer);

        return savedOrganization.toDto();
    }

    @Transactional(readOnly = true)
    public Optional<UserAuthDto> findOrganizerAuthByEmail(String email) {
        return organizerRepository.findByEmail(email)
                .map(organizer -> new UserAuthDto(organizer.getEmail(), organizer.getPassword(), "ROLE_ORGANIZER"));
    }
}
