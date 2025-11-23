package com.github.fjbaldon.attendex.platform.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fjbaldon.attendex.platform.organization.events.OrganizationRegisteredEvent;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@RequiredArgsConstructor
class NotificationDispatcher {

    private final ObjectMapper objectMapper;
    private final EmailService emailService;

    void dispatch(NotificationOutbox notification) throws IOException {
        String eventType = notification.getEventType();
        String payload = notification.getPayload();

        if (OrganizationRegisteredEvent.class.getName().equals(eventType)) {
            OrganizationRegisteredEvent event = objectMapper.readValue(payload, OrganizationRegisteredEvent.class);
            emailService.sendVerificationEmail(
                    event.organizerEmail(),
                    event.organizationName(),
                    event.verificationToken()
            );
        } else {
            throw new IllegalStateException("Unknown event type in notification outbox: " + eventType);
        }
    }
}
