package com.domus.server.messaging.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ConversationResponse(
    UUID id,
    MessagingUserSummaryResponse otherParticipant,
    String lastMessagePreview,
    boolean lastMessageFromCurrentUser,
    long unreadCount,
    Instant lastMessageAt
) {
}
