package com.domus.server.messaging.repository;

import com.domus.server.messaging.entity.MessageEntity;
import com.domus.server.messaging.entity.MessageStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<MessageEntity, UUID> {

    @Query("""
        select m
        from MessageEntity m
        join fetch m.sender
        join fetch m.recipient
        join fetch m.conversation
        where m.conversation.id = :conversationId
        order by m.createdAt asc
        """)
    List<MessageEntity> findAllByConversationId(@Param("conversationId") UUID conversationId);

    long countByConversationIdAndRecipientIdAndStatus(UUID conversationId, UUID recipientId, MessageStatus status);
}
