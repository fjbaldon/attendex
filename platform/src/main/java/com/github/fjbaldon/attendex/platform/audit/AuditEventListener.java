package com.github.fjbaldon.attendex.platform.audit;

import com.github.fjbaldon.attendex.platform.admin.events.StewardCreatedEvent;
import com.github.fjbaldon.attendex.platform.admin.events.StewardDeletedEvent;
import com.github.fjbaldon.attendex.platform.organization.events.OrganizationLifecycleChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
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
                Map.of(
                        "organizationId", event.organizationId(),
                        "from", event.previousLifecycle(),
                        "to", event.newLifecycle()
                )
        );
        auditRepository.save(audit);
    }
}
