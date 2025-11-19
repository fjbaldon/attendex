package com.github.fjbaldon.attendex.platform.audit;

import com.github.fjbaldon.attendex.platform.admin.events.StewardCreatedEvent;
import com.github.fjbaldon.attendex.platform.admin.events.StewardDeletedEvent;
import com.github.fjbaldon.attendex.platform.identity.events.UserLoggedInEvent;
import com.github.fjbaldon.attendex.platform.identity.events.UserPasswordChangedEvent;
import com.github.fjbaldon.attendex.platform.organization.events.OrganizationLifecycleChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
class AuditEventListener {

    private final AuditRepository auditRepository;

    @Async
    @EventListener
    public void onStewardCreated(StewardCreatedEvent event) {
        Audit audit = Audit.record(
                event.actorEmail(),
                "STEWARD_CREATED",
                "SUCCESS",
                null,
                Map.of("targetEmail", event.targetStewardEmail())
        );
        auditRepository.save(audit);
    }

    @Async
    @EventListener
    public void onStewardDeleted(StewardDeletedEvent event) {
        Audit audit = Audit.record(
                event.actorEmail(),
                "STEWARD_DELETED",
                "SUCCESS",
                null,
                Map.of("targetEmail", event.targetStewardEmail())
        );
        auditRepository.save(audit);
    }

    @Async
    @EventListener
    public void onOrganizationLifecycleChanged(OrganizationLifecycleChangedEvent event) {
        Audit audit = Audit.record(
                event.actorEmail(),
                "ORGANIZATION_LIFECYCLE_CHANGED",
                "SUCCESS",
                null,
                Map.of(
                        "organizationId", event.organizationId(),
                        "from", event.previousLifecycle(),
                        "to", event.newLifecycle()
                )
        );
        auditRepository.save(audit);
    }

    @Async
    @EventListener
    public void onUserLoggedIn(UserLoggedInEvent event) {
        Audit audit = Audit.record(
                event.email(),
                "USER_LOGIN_SUCCESS",
                "SUCCESS",
                event.ipAddress(),
                null
        );
        auditRepository.save(audit);
    }

    @Async
    @EventListener
    public void onUserPasswordChanged(UserPasswordChangedEvent event) {
        Audit audit = Audit.record(
                event.email(),
                "USER_PASSWORD_CHANGED",
                "SUCCESS",
                event.ipAddress(),
                null
        );
        auditRepository.save(audit);
    }

    @Async
    @EventListener
    public void onLoginFailure(AuthenticationFailureBadCredentialsEvent event) {
        String email = (String) event.getAuthentication().getPrincipal();
        // Getting IP from event is tricky in async; normally passed via WebAuthenticationDetails
        // For simplicity, we might log "N/A" or need a synchronous approach.
        // Or, assume 'details' contains IP if WebAuthenticationDetailsSource was used.

        Audit audit = Audit.record(
                email,
                "USER_LOGIN_FAILURE",
                "FAILURE",
                "N/A", // extracting IP from async event requires extraction in Controller or Filter
                Map.of("error", event.getException().getMessage())
        );
    }
}
