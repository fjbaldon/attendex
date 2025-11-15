package com.github.fjbaldon.attendex.platform.analytics;

import com.github.fjbaldon.attendex.platform.capture.events.EntryCreatedEvent;
import com.github.fjbaldon.attendex.platform.event.events.EventCreatedEvent;
import com.github.fjbaldon.attendex.platform.event.events.RosterEntryAddedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class AnalyticsEventListener {

    private final OrganizationSummaryRepository orgRepo;
    private final EventSummaryRepository eventRepo;

    @Async
    @EventListener
    public void onEventCreated(EventCreatedEvent event) {
        // Implementation to increment organization's event count
        // and create a new event summary.
    }

    @Async
    @EventListener
    public void onRosterEntryAdded(RosterEntryAddedEvent event) {
        // Implementation to increment event's roster count.
    }

    @Async
    @EventListener
    public void onEntryCreated(EntryCreatedEvent event) {
        // Implementation to increment event's entry count.
    }
}
