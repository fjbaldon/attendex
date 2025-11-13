package com.github.fjbaldon.attendex.platform.notification;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

interface NotificationOutboxRepository extends CrudRepository<NotificationOutbox, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT n FROM NotificationOutbox n WHERE n.status = 'PENDING' OR n.status = 'FAILED' ORDER BY n.createdAt ASC LIMIT 10")
    List<NotificationOutbox> findProcessableNotifications();
}
