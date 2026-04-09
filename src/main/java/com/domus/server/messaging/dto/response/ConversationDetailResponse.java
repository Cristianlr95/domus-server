package com.domus.server.messaging.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ConversationDetailResponse(
    UUID id,
    MessagingUserSummaryResponse currentUser,
    MessagingUserSummaryResponse otherParticipant,
    List<MessageResponse> messages,
    long unreadCount,
    Instant lastMessageAt
) {
}
