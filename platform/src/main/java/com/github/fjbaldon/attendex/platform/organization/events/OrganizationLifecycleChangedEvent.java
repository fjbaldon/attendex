package com.github.fjbaldon.attendex.platform.organization.events;

public record OrganizationLifecycleChangedEvent(
        String actorEmail,
        Long organizationId,
        String previousLifecycle,
        String newLifecycle
) {
}
