package com.domus.server.messaging.mapper;

import com.domus.server.messaging.dto.response.MessageResponse;
import com.domus.server.messaging.dto.response.MessagingUserSummaryResponse;
import com.domus.server.messaging.entity.MessageEntity;
import com.domus.server.user.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class MessagingMapper {

    public MessagingUserSummaryResponse toUserSummary(UserEntity user) {
        return new MessagingUserSummaryResponse(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getRoleNames()
        );
    }

    public MessageResponse toMessageResponse(MessageEntity message) {
        return new MessageResponse(
            message.getId(),
            message.getConversation().getId(),
            toUserSummary(message.getSender()),
            toUserSummary(message.getRecipient()),
            message.getContent(),
            message.getStatus(),
            message.getCreatedAt(),
            message.getUpdatedAt(),
            message.getReadAt()
        );
    }
}
