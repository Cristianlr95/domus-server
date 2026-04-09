package com.domus.server.notifications.dto.response;

import com.domus.server.notifications.entity.NotificationType;
import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
    UUID id,
    NotificationType type,
    String title,
    String message,
    boolean read,
    Instant readAt,
    String referenceType,
    UUID referenceId,
    String route,
    Instant createdAt,
    Instant updatedAt
) {
}
