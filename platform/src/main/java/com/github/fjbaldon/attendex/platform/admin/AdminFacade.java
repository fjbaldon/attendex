package com.github.fjbaldon.attendex.platform.admin;

import com.github.fjbaldon.attendex.platform.organization.OrganizationFacade;
import com.github.fjbaldon.attendex.platform.organization.OrganizationDto;
import com.github.fjbaldon.attendex.platform.organization.OrganizationLifecycleChangedEvent;
import com.github.fjbaldon.attendex.platform.organization.PasswordResetInitiatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminFacade {

    private final StewardRepository stewardRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrganizationFacade organizationFacade;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public StewardDto createSteward(CreateStewardRequestDto request) {
        Assert.isTrue(!stewardRepository.existsByEmail(request.email()), "A steward with this email already exists.");
        String encodedPassword = passwordEncoder.encode(request.password());
        Steward steward = Steward.create(request.email(), encodedPassword);
        Steward saved = stewardRepository.save(steward);

        String actorEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        eventPublisher.publishEvent(new StewardCreatedEvent(actorEmail, saved.getEmail()));

        return new StewardDto(saved.getId(), saved.getEmail(), saved.getCreatedAt());
    }

    @Transactional
    public void deleteSteward(Long stewardId, String currentStewardEmail) {
        Steward stewardToDelete = stewardRepository.findById(stewardId)
                .orElseThrow(() -> new UsernameNotFoundException("Steward not found with ID: " + stewardId));

        Assert.isTrue(!stewardToDelete.getEmail().equals(currentStewardEmail), "You cannot delete your own account.");
        Assert.isTrue(stewardRepository.count() > 1, "Cannot delete the last steward.");

        stewardRepository.delete(stewardToDelete);
        eventPublisher.publishEvent(new StewardDeletedEvent(currentStewardEmail, stewardToDelete.getEmail()));
    }

    @Transactional
    public OrganizationDto updateOrganizationLifecycle(Long organizationId, UpdateOrganizationLifecycleDto dto) {
        OrganizationDto previousState = organizationFacade.findOrganizationById(organizationId);
        OrganizationDto newState = organizationFacade.updateLifecycle(organizationId, dto.lifecycle());

        String actorEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        eventPublisher.publishEvent(new OrganizationLifecycleChangedEvent(
                actorEmail,
                organizationId,
                previousState.lifecycle(),
                newState.lifecycle()
        ));

        return newState;
    }

    @Transactional
    public OrganizationDto updateOrganizationSubscription(Long organizationId, UpdateSubscriptionDto dto) {
        return organizationFacade.updateSubscription(organizationId, dto.subscriptionType(), dto.expiresAt());
    }

    public void createDefaultStewardIfNeeded() {
        if (stewardRepository.count() == 0) {
            Steward defaultSteward = Steward.create("admin@attendex.com", passwordEncoder.encode("password"));
            stewardRepository.save(defaultSteward);
        }
    }

    @Transactional(readOnly = true)
    public Page<StewardDto> findAllStewards(Pageable pageable) {
        return stewardRepository.findAll(pageable)
                .map(steward -> new StewardDto(
                        steward.getId(),
                        steward.getEmail(),
                        steward.getCreatedAt()
                ));
    }

    @Transactional(readOnly = true)
    public Optional<UserAuthDto> findStewardAuthByEmail(String email) {
        return stewardRepository.findByEmail(email)
                .map(steward -> new UserAuthDto(
                        steward.getEmail(),
                        steward.getPassword(),
                        "ROLE_STEWARD",
                        steward.isForcePasswordChange()
                ));
    }

    @Transactional
    public void changeStewardPassword(String email, String newPassword) {
        Assert.hasText(email, "Email cannot be blank");
        Assert.hasText(newPassword, "New password cannot be blank");

        Steward steward = stewardRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Steward not found with email: " + email));

        String encodedPassword = passwordEncoder.encode(newPassword);
        steward.changePassword(encodedPassword);
    }

    @Transactional
    public void resetStewardPassword(Long stewardId, String newPassword) {
        Steward steward = stewardRepository.findById(stewardId)
                .orElseThrow(() -> new UsernameNotFoundException("Steward not found with ID: " + stewardId));

        String encodedPassword = passwordEncoder.encode(newPassword);
        steward.changePassword(encodedPassword);

        // Optional: Fire event if you want to email them, though for Stewards you might just tell them directly.
        // Keeping it consistent with the rest of the app:
        eventPublisher.publishEvent(new PasswordResetInitiatedEvent(
                steward.getEmail(),
                newPassword,
                "Platform Administration" // Organization Name context
        ));
    }
}
