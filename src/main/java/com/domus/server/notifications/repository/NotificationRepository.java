package com.domus.server.notifications.repository;

import com.domus.server.notifications.entity.NotificationEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    List<NotificationEntity> findAllByRecipientUserIdOrderByCreatedAtDesc(UUID recipientUserId);

    List<NotificationEntity> findAllByRecipientUserIdAndReadFalseOrderByCreatedAtDesc(UUID recipientUserId);

    long countByRecipientUserIdAndReadFalse(UUID recipientUserId);
}
