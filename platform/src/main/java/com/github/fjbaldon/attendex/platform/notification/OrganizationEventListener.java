package com.github.fjbaldon.attendex.platform.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fjbaldon.attendex.platform.organization.events.OrganizationRegisteredEvent;
import com.github.fjbaldon.attendex.platform.organization.events.PasswordResetInitiatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
class OrganizationEventListener {

    private final NotificationOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @EventListener
    @Transactional
    public void handleOrganizationRegisteredEvent(OrganizationRegisteredEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            NotificationOutbox outboxItem = NotificationOutbox.create(event, payload);
            outboxRepository.save(outboxItem);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize OrganizationRegisteredEvent for outbox", e);
        }
    }

    @EventListener
    @Transactional
    public void handlePasswordResetInitiatedEvent(PasswordResetInitiatedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            NotificationOutbox outboxItem = NotificationOutbox.create(event, payload);
            outboxRepository.save(outboxItem);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize PasswordResetInitiatedEvent for outbox", e);
        }
    }
}
