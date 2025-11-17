package com.github.fjbaldon.attendex.platform.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;

@Configuration
@ConditionalOnProperty(name = "spring.mail.host")
class NotificationConfiguration {

    @Bean
    EmailService emailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        return new EmailService(mailSender, templateEngine);
    }

    @Bean
    OrganizationEventListener organizationEventListener(NotificationOutboxRepository outboxRepository, ObjectMapper objectMapper) {
        return new OrganizationEventListener(outboxRepository, objectMapper);
    }

    @Bean
    NotificationDispatcher notificationDispatcher(ObjectMapper objectMapper, EmailService emailService) {
        return new NotificationDispatcher(objectMapper, emailService);
    }

    @Bean
    NotificationScheduler notificationScheduler(NotificationOutboxRepository outboxRepository, NotificationDispatcher dispatcher) {
        return new NotificationScheduler(outboxRepository, dispatcher);
    }
}
