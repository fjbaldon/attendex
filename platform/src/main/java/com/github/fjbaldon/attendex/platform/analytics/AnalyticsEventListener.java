package com.github.fjbaldon.attendex.platform.analytics;

import com.github.fjbaldon.attendex.platform.capture.events.EntryCreatedEvent;
import com.github.fjbaldon.attendex.platform.event.events.EventCreatedEvent;
import com.github.fjbaldon.attendex.platform.event.events.RosterEntryAddedEvent;
import com.github.fjbaldon.attendex.platform.organization.events.OrganizationRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
class AnalyticsEventListener {

    private final OrganizationSummaryRepository orgSummaryRepository;
    private final EventSummaryRepository eventSummaryRepository;

    @Async
    @EventListener
    @Transactional
    public void onOrganizationRegistered(OrganizationRegisteredEvent event) {
        // When a new organization is created, create its initial summary record.
        // This is not strictly required by the dashboard but is good practice.
    }

    @Async
    @EventListener
    @Transactional
    public void onEventCreated(EventCreatedEvent event) {
        OrganizationSummary orgSummary = orgSummaryRepository.findById(event.organizationId())
                .orElse(new OrganizationSummary(event.organizationId()));
        orgSummary.incrementEventCount();
        orgSummaryRepository.save(orgSummary);

        EventSummary eventSummary = new EventSummary(event.eventId(), event.organizationId(), event.eventName());
        eventSummaryRepository.save(eventSummary);
    }

    @Async
    @EventListener
    @Transactional
    public void onRosterEntryAdded(RosterEntryAddedEvent event) {
        eventSummaryRepository.findById(event.eventId()).ifPresent(EventSummary::incrementRosterCount);
    }

    @Async
    @EventListener
    @Transactional
    public void onEntryCreated(EntryCreatedEvent event) {
        eventSummaryRepository.findById(event.eventId()).ifPresent(EventSummary::incrementEntryCount);
    }
}
