package com.github.fjbaldon.attendex.platform.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
class NotificationScheduler {

    private final NotificationOutboxRepository outboxRepository;
    private final NotificationDispatcher dispatcher;
    private static final int MAX_RETRIES = 5;

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void processNotificationOutbox() {
        List<NotificationOutbox> notifications = outboxRepository.findProcessableNotifications();

        if (notifications.isEmpty()) {
            return;
        }

        log.info("Found {} notifications to process.", notifications.size());

        for (NotificationOutbox notification : notifications) {
            try {
                if (notification.getRetryCount() >= MAX_RETRIES) {
                    log.warn("Notification {} has exceeded max retries and will be skipped.", notification.getId());
                    notification.markAsProcessed();
                    continue;
                }
                dispatcher.dispatch(notification);
                notification.markAsProcessed();
                log.info("Successfully processed notification {}.", notification.getId());
            } catch (Exception e) {
                log.error("Failed to process notification {}. It will be retried.", notification.getId(), e);
                notification.incrementRetryCountAndMarkAsFailed();
            }
        }
    }
}
