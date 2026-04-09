package com.domus.server.messaging.service;

import com.domus.server.common.exception.ResourceNotFoundException;
import com.domus.server.messaging.dto.request.SendMessageRequest;
import com.domus.server.messaging.dto.response.ConversationDetailResponse;
import com.domus.server.messaging.dto.response.ConversationResponse;
import com.domus.server.messaging.dto.response.MessageResponse;
import com.domus.server.messaging.dto.response.MessagingUserSummaryResponse;
import com.domus.server.messaging.entity.ConversationEntity;
import com.domus.server.messaging.entity.MessageEntity;
import com.domus.server.messaging.entity.MessageStatus;
import com.domus.server.messaging.mapper.MessagingMapper;
import com.domus.server.messaging.repository.ConversationRepository;
import com.domus.server.messaging.repository.MessageRepository;
import com.domus.server.user.entity.UserEntity;
import com.domus.server.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MessagingService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MessagingMapper messagingMapper;

    public MessagingService(
        ConversationRepository conversationRepository,
        MessageRepository messageRepository,
        UserRepository userRepository,
        MessagingMapper messagingMapper
    ) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.messagingMapper = messagingMapper;
    }

    public MessageResponse sendMessage(UUID senderUserId, SendMessageRequest request) {
        if (senderUserId.equals(request.recipientUserId())) {
            throw new IllegalArgumentException("You cannot send a message to yourself.");
        }

        UserEntity sender = getActiveUser(senderUserId);
        UserEntity recipient = getActiveUser(request.recipientUserId());

        ConversationEntity conversation = getOrCreateConversation(sender, recipient);

        MessageEntity message = new MessageEntity();
        message.setId(UUID.randomUUID());
        message.setConversation(conversation);
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(request.content().trim());
        message.setStatus(MessageStatus.ENVIADO);

        MessageEntity savedMessage = messageRepository.save(message);
        updateConversationMetadata(conversation, sender, savedMessage.getContent(), savedMessage.getCreatedAt());
        return messagingMapper.toMessageResponse(savedMessage);
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> listMessages(UUID currentUserId, UUID conversationId) {
        ConversationEntity conversation = getConversationForUser(conversationId, currentUserId);
        return messageRepository.findAllByConversationId(conversation.getId()).stream()
            .map(messagingMapper::toMessageResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> listConversations(UUID currentUserId) {
        return conversationRepository.findAllForUser(currentUserId).stream()
            .map(conversation -> toConversationResponse(conversation, currentUserId))
            .toList();
    }

    @Transactional(readOnly = true)
    public ConversationDetailResponse getConversationDetail(UUID currentUserId, UUID conversationId) {
        ConversationEntity conversation = getConversationForUser(conversationId, currentUserId);
        UserEntity currentUser = getUser(currentUserId);
        UserEntity otherParticipant = getOtherParticipant(conversation, currentUserId);
        List<MessageResponse> messages = messageRepository.findAllByConversationId(conversationId).stream()
            .map(messagingMapper::toMessageResponse)
            .toList();

        return new ConversationDetailResponse(
            conversation.getId(),
            messagingMapper.toUserSummary(currentUser),
            messagingMapper.toUserSummary(otherParticipant),
            messages,
            messageRepository.countByConversationIdAndRecipientIdAndStatus(
                conversation.getId(),
                currentUserId,
                MessageStatus.ENVIADO
            ),
            conversation.getLastMessageAt()
        );
    }

    public MessageResponse markAsRead(UUID currentUserId, UUID messageId) {
        MessageEntity message = messageRepository.findById(messageId)
            .orElseThrow(() -> new ResourceNotFoundException("Message not found."));

        if (!message.getRecipient().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("You can only mark your own received messages as read.");
        }

        if (message.getStatus() == MessageStatus.LEIDO) {
            return messagingMapper.toMessageResponse(message);
        }

        message.setStatus(MessageStatus.LEIDO);
        message.setReadAt(Instant.now());
        return messagingMapper.toMessageResponse(messageRepository.save(message));
    }

    @Transactional(readOnly = true)
    public List<MessagingUserSummaryResponse> listContacts(UUID currentUserId) {
        return userRepository.findAllActiveExcluding(currentUserId).stream()
            .map(messagingMapper::toUserSummary)
            .toList();
    }

    private ConversationEntity getOrCreateConversation(UserEntity sender, UserEntity recipient) {
        UUID firstId = sender.getId().compareTo(recipient.getId()) <= 0 ? sender.getId() : recipient.getId();
        UUID secondId = sender.getId().compareTo(recipient.getId()) <= 0 ? recipient.getId() : sender.getId();

        return conversationRepository.findByParticipantOneIdAndParticipantTwoId(firstId, secondId)
            .orElseGet(() -> {
                ConversationEntity conversation = new ConversationEntity();
                conversation.setId(UUID.randomUUID());
                conversation.setParticipantOne(firstId.equals(sender.getId()) ? sender : recipient);
                conversation.setParticipantTwo(secondId.equals(recipient.getId()) ? recipient : sender);
                conversation.setLastMessageAt(Instant.now());
                return conversationRepository.save(conversation);
            });
    }

    private void updateConversationMetadata(
        ConversationEntity conversation,
        UserEntity sender,
        String content,
        Instant createdAt
    ) {
        conversation.setLastMessageSender(sender);
        conversation.setLastMessagePreview(content.length() > 255 ? content.substring(0, 255) : content);
        conversation.setLastMessageAt(createdAt);
        conversationRepository.save(conversation);
    }

    private ConversationEntity getConversationForUser(UUID conversationId, UUID currentUserId) {
        ConversationEntity conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new ResourceNotFoundException("Conversation not found."));

        if (!conversation.getParticipantOne().getId().equals(currentUserId)
            && !conversation.getParticipantTwo().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("You do not have access to this conversation.");
        }

        return conversation;
    }

    private ConversationResponse toConversationResponse(ConversationEntity conversation, UUID currentUserId) {
        UserEntity otherParticipant = getOtherParticipant(conversation, currentUserId);
        long unreadCount = messageRepository.countByConversationIdAndRecipientIdAndStatus(
            conversation.getId(),
            currentUserId,
            MessageStatus.ENVIADO
        );

        return new ConversationResponse(
            conversation.getId(),
            messagingMapper.toUserSummary(otherParticipant),
            conversation.getLastMessagePreview(),
            conversation.getLastMessageSender() != null
                && conversation.getLastMessageSender().getId().equals(currentUserId),
            unreadCount,
            conversation.getLastMessageAt()
        );
    }

    private UserEntity getOtherParticipant(ConversationEntity conversation, UUID currentUserId) {
        return conversation.getParticipantOne().getId().equals(currentUserId)
            ? conversation.getParticipantTwo()
            : conversation.getParticipantOne();
    }

    private UserEntity getActiveUser(UUID userId) {
        UserEntity user = getUser(userId);
        if (!user.isActive()) {
            throw new IllegalArgumentException("The selected user is inactive.");
        }
        return user;
    }

    private UserEntity getUser(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }
}
