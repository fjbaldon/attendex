package com.github.fjbaldon.attendex.platform.analytics;

import com.github.fjbaldon.attendex.platform.attendee.AttendeeFacade;
import com.github.fjbaldon.attendex.platform.capture.events.EntriesSyncedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
class CaptureEventListener {

    private final AttendeeFacade attendeeFacade;
    private final AttributeBreakdownRepository repository;

    @Async
    @EventListener
    @Transactional
    public void handleEntriesSyncedEvent(EntriesSyncedEvent event) {
        event.attendeeIdsByEventId().forEach((eventId, attendeeIds) -> {
            var attendees = attendeeFacade.findAttendeesByIds(attendeeIds);
            attendees.forEach(attendee -> {
                if (attendee.attributes() != null) {
                    attendee.attributes().forEach((name, value) -> updateBreakdown(eventId, name, value.toString()));
                }
            });
        });
    }

    private void updateBreakdown(Long eventId, String name, String value) {
        repository.findByEventIdAndAttributeNameAndAttributeValue(eventId, name, value)
                .ifPresentOrElse(
                        AttributeBreakdown::incrementCount,
                        () -> repository.save(AttributeBreakdown.create(eventId, name, value))
                );
    }
}