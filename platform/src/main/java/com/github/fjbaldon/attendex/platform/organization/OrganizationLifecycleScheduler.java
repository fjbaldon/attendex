package com.github.fjbaldon.attendex.platform.organization;

import com.github.fjbaldon.attendex.platform.organization.events.OrganizationLifecycleChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
class OrganizationLifecycleScheduler {

    private final OrganizationRepository organizationRepository;
    private final ApplicationEventPublisher eventPublisher;

    // Run every hour to catch expirations relatively quickly
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void processExpiredSubscriptions() {
        Instant now = Instant.now();

        // Find all organizations that are ACTIVE but have expired
        List<Organization> expiredOrgs = organizationRepository.findByLifecycleAndSubscriptionExpiresAtBefore("ACTIVE", now);

        if (expiredOrgs.isEmpty()) {
            return;
        }

        log.info("Found {} organizations with expired subscriptions. Suspending access...", expiredOrgs.size());

        for (Organization org : expiredOrgs) {
            String oldLifecycle = org.getLifecycle();

            // Transition state
            org.updateLifecycle("SUSPENDED");
            organizationRepository.save(org);

            // Publish event for Audit Log (Actor is 'SYSTEM')
            eventPublisher.publishEvent(new OrganizationLifecycleChangedEvent(
                    "SYSTEM_SCHEDULER",
                    org.getId(),
                    oldLifecycle,
                    "SUSPENDED"
            ));

            log.info("Suspended Organization ID {} due to subscription expiry.", org.getId());
        }
    }
}
