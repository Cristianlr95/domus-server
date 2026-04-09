package com.domus.server.messaging.dto.response;

import com.domus.server.messaging.entity.MessageStatus;
import java.time.Instant;
import java.util.UUID;

public record MessageResponse(
    UUID id,
    UUID conversationId,
    MessagingUserSummaryResponse sender,
    MessagingUserSummaryResponse recipient,
    String content,
    MessageStatus status,
    Instant createdAt,
    Instant updatedAt,
    Instant readAt
) {
}
