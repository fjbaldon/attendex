package com.github.fjbaldon.attendex.platform.analytics;

import com.github.fjbaldon.attendex.platform.attendee.AttendeeBatchDeletedEvent;
import com.github.fjbaldon.attendex.platform.attendee.AttendeeCreatedEvent;
import com.github.fjbaldon.attendex.platform.attendee.AttendeeDeletedEvent;
import com.github.fjbaldon.attendex.platform.attendee.AttributeDeletedEvent;
import com.github.fjbaldon.attendex.platform.capture.EntryCreatedEvent;
import com.github.fjbaldon.attendex.platform.event.EventCreatedEvent;
import com.github.fjbaldon.attendex.platform.event.RosterEntryAddedEvent;
import com.github.fjbaldon.attendex.platform.organization.ScannerCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class AnalyticsEventListener {

    private final OrganizationSummaryRepository orgSummaryRepository;
    private final EventSummaryRepository eventSummaryRepository;
    private final SessionSummaryRepository sessionSummaryRepository;
    private final ScannerSummaryRepository scannerSummaryRepository;
    private final AttributeBreakdownRepository attributeBreakdownRepository;

    @ApplicationModuleListener
    public void onEntryCreated(EntryCreatedEvent event) {
        // 1. Update Event Total
        eventSummaryRepository.incrementEntryCount(event.eventId());

        // 2. Update Session Total
        if (event.sessionId() != null) {
            sessionSummaryRepository.incrementCount(event.sessionId(), event.eventId());
        }

        // 3. Update Scanner Total
        if (event.scannerId() != null) {
            scannerSummaryRepository.incrementCount(event.scannerId(), event.eventId());
        }
    }

    @ApplicationModuleListener
    public void onEventCreated(EventCreatedEvent event) {
        orgSummaryRepository.insertIfNotExists(event.organizationId());
        orgSummaryRepository.incrementEventCount(event.organizationId());

        EventSummary eventSummary = new EventSummary(event.eventId(), event.organizationId(), event.eventName());
        eventSummaryRepository.save(eventSummary);
    }

    @ApplicationModuleListener
    public void onRosterEntryAdded(RosterEntryAddedEvent event) {
        eventSummaryRepository.incrementRosterCount(event.eventId());
    }

    @ApplicationModuleListener
    public void onAttendeeCreated(AttendeeCreatedEvent event) {
        orgSummaryRepository.insertIfNotExists(event.organizationId());
        orgSummaryRepository.incrementAttendeeCount(event.organizationId());
    }

    @ApplicationModuleListener
    public void onScannerCreated(ScannerCreatedEvent event) {
        orgSummaryRepository.insertIfNotExists(event.organizationId());
        orgSummaryRepository.incrementScannerCount(event.organizationId());
    }

    @ApplicationModuleListener
    public void onAttendeeDeleted(AttendeeDeletedEvent event) {
        orgSummaryRepository.decrementAttendeeCount(event.organizationId());
    }

    @ApplicationModuleListener
    public void onAttendeeBatchDeleted(AttendeeBatchDeletedEvent event) {
        orgSummaryRepository.decrementAttendeeCountBy(event.organizationId(), event.count());
    }

    @ApplicationModuleListener
    public void onAttributeDeleted(AttributeDeletedEvent event) {
        attributeBreakdownRepository.deleteStatsForAttribute(event.organizationId(), event.attributeName());
    }
}
