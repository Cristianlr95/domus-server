package com.domus.server.notifications.service;

import com.domus.server.common.exception.ResourceNotFoundException;
import com.domus.server.messaging.entity.MessageEntity;
import com.domus.server.notifications.dto.response.NotificationResponse;
import com.domus.server.notifications.dto.response.NotificationUnreadCountResponse;
import com.domus.server.notifications.entity.NotificationEntity;
import com.domus.server.notifications.entity.NotificationType;
import com.domus.server.notifications.mapper.NotificationMapper;
import com.domus.server.notifications.repository.NotificationRepository;
import com.domus.server.packages.entity.PackageEntity;
import com.domus.server.user.entity.UserEntity;
import com.domus.server.user.repository.UserRepository;
import com.domus.server.visits.entity.VisitEntity;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    public NotificationService(
        NotificationRepository notificationRepository,
        UserRepository userRepository,
        NotificationMapper notificationMapper
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.notificationMapper = notificationMapper;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listForUser(UUID userId, Boolean unreadOnly) {
        List<NotificationEntity> notifications = Boolean.TRUE.equals(unreadOnly)
            ? notificationRepository.findAllByRecipientUserIdAndReadFalseOrderByCreatedAtDesc(userId)
            : notificationRepository.findAllByRecipientUserIdOrderByCreatedAtDesc(userId);

        return notifications.stream()
            .map(notificationMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public NotificationUnreadCountResponse getUnreadCount(UUID userId) {
        return new NotificationUnreadCountResponse(notificationRepository.countByRecipientUserIdAndReadFalse(userId));
    }

    public NotificationResponse markAsRead(UUID userId, UUID notificationId) {
        NotificationEntity notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification not found."));

        if (!notification.getRecipientUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only mark your own notifications as read.");
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(Instant.now());
            notification = notificationRepository.save(notification);
        }

        return notificationMapper.toResponse(notification);
    }

    public void notifyPackageReceived(PackageEntity packageEntity) {
        if (packageEntity.getResidentUser() == null) {
            return;
        }

        String subtitle = packageEntity.getUnitLabel() != null
            ? " para la unidad " + packageEntity.getUnitLabel()
            : "";

        createNotification(
            packageEntity.getResidentUser().getId(),
            NotificationType.PACKAGE_RECEIVED,
            "Nueva encomienda recibida",
            "Se registro " + packageEntity.getDescription() + subtitle + ".",
            "PACKAGE",
            packageEntity.getId(),
            "/packages/" + packageEntity.getId()
        );
    }

    public void notifyVisitRegistered(VisitEntity visit) {
        if (visit.getResidentUser() == null) {
            return;
        }

        createNotification(
            visit.getResidentUser().getId(),
            NotificationType.VISIT_REGISTERED,
            "Nueva visita registrada",
            "Se registro una visita de " + visit.getVisitorName() + ".",
            "VISIT",
            visit.getId(),
            "/visits/" + visit.getId()
        );
    }

    public void notifyMessageReceived(MessageEntity message) {
        createNotification(
            message.getRecipient().getId(),
            NotificationType.MESSAGE_RECEIVED,
            "Nuevo mensaje interno",
            message.getSender().getFirstName() + " " + message.getSender().getLastName() + " te envio un mensaje.",
            "CONVERSATION",
            message.getConversation().getId(),
            "/messaging/" + message.getConversation().getId()
        );
    }

    public void createNotification(
        UUID recipientUserId,
        NotificationType type,
        String title,
        String message,
        String referenceType,
        UUID referenceId,
        String route
    ) {
        UserEntity recipientUser = userRepository.findById(recipientUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification recipient not found."));

        NotificationEntity notification = new NotificationEntity();
        notification.setId(UUID.randomUUID());
        notification.setRecipientUser(recipientUser);
        notification.setType(type);
        notification.setTitle(title.trim());
        notification.setMessage(message.trim());
        notification.setRead(false);
        notification.setReferenceType(referenceType);
        notification.setReferenceId(referenceId);
        notification.setRoute(route);
        notificationRepository.save(notification);
    }
}
