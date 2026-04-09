CREATE TABLE conversations (
    id UUID PRIMARY KEY,
    participant_one_id UUID NOT NULL,
    participant_two_id UUID NOT NULL,
    last_message_sender_id UUID,
    last_message_preview VARCHAR(255),
    last_message_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_conversations_participant_one FOREIGN KEY (participant_one_id) REFERENCES users(id),
    CONSTRAINT fk_conversations_participant_two FOREIGN KEY (participant_two_id) REFERENCES users(id),
    CONSTRAINT fk_conversations_last_message_sender FOREIGN KEY (last_message_sender_id) REFERENCES users(id),
    CONSTRAINT uk_conversations_participants UNIQUE (participant_one_id, participant_two_id)
);

CREATE TABLE messages (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL,
    sender_user_id UUID NOT NULL,
    recipient_user_id UUID NOT NULL,
    content VARCHAR(2000) NOT NULL,
    status VARCHAR(20) NOT NULL,
    read_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_messages_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id),
    CONSTRAINT fk_messages_sender FOREIGN KEY (sender_user_id) REFERENCES users(id),
    CONSTRAINT fk_messages_recipient FOREIGN KEY (recipient_user_id) REFERENCES users(id)
);

CREATE INDEX idx_conversations_last_message_at ON conversations(last_message_at);
CREATE INDEX idx_conversations_participant_one ON conversations(participant_one_id);
CREATE INDEX idx_conversations_participant_two ON conversations(participant_two_id);
CREATE INDEX idx_messages_conversation ON messages(conversation_id);
CREATE INDEX idx_messages_recipient_status ON messages(recipient_user_id, status);
CREATE INDEX idx_messages_created_at ON messages(created_at);
