package com.github.fjbaldon.attendex.platform.analytics;

import com.github.fjbaldon.attendex.platform.attendee.events.AttendeeCreatedEvent;
import com.github.fjbaldon.attendex.platform.attendee.events.AttendeeDeletedEvent;
import com.github.fjbaldon.attendex.platform.capture.events.EntryCreatedEvent;
import com.github.fjbaldon.attendex.platform.event.events.EventCreatedEvent;
import com.github.fjbaldon.attendex.platform.event.events.RosterEntryAddedEvent;
import com.github.fjbaldon.attendex.platform.organization.events.ScannerCreatedEvent;
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

    private OrganizationSummary findOrCreateOrgSummary(Long organizationId) {
        return orgSummaryRepository.findById(organizationId)
                .orElse(new OrganizationSummary(organizationId));
    }

    @Async
    @EventListener
    @Transactional
    public void onEventCreated(EventCreatedEvent event) {
        OrganizationSummary orgSummary = findOrCreateOrgSummary(event.organizationId());
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

    @Async
    @EventListener
    @Transactional
    public void onAttendeeCreated(AttendeeCreatedEvent event) {
        OrganizationSummary orgSummary = findOrCreateOrgSummary(event.organizationId());
        orgSummary.incrementAttendeeCount();
        orgSummaryRepository.save(orgSummary);
    }

    @Async
    @EventListener
    @Transactional
    public void onScannerCreated(ScannerCreatedEvent event) {
        OrganizationSummary orgSummary = findOrCreateOrgSummary(event.organizationId());
        orgSummary.incrementScannerCount();
        orgSummaryRepository.save(orgSummary);
    }

    @Async
    @EventListener
    @Transactional
    public void onAttendeeDeleted(AttendeeDeletedEvent event) {
        // We use a custom query or find-modify-save to decrement
        orgSummaryRepository.findById(event.organizationId())
                .ifPresent(summary -> {
                    // Assuming you add a decrement method to the Entity
                    summary.decrementAttendeeCount();
                    orgSummaryRepository.save(summary);
                });
    }
}
