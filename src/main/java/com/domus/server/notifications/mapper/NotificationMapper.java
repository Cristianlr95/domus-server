package com.domus.server.notifications.mapper;

import com.domus.server.notifications.dto.response.NotificationResponse;
import com.domus.server.notifications.entity.NotificationEntity;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(NotificationEntity notification) {
        return new NotificationResponse(
            notification.getId(),
            notification.getType(),
            notification.getTitle(),
            notification.getMessage(),
            notification.isRead(),
            notification.getReadAt(),
            notification.getReferenceType(),
            notification.getReferenceId(),
            notification.getRoute(),
            notification.getCreatedAt(),
            notification.getUpdatedAt()
        );
    }
}
