CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    recipient_user_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(150) NOT NULL,
    message VARCHAR(500) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP WITH TIME ZONE,
    reference_type VARCHAR(50),
    reference_id UUID,
    route VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_notifications_recipient_user FOREIGN KEY (recipient_user_id) REFERENCES users(id)
);

CREATE INDEX idx_notifications_recipient_created_at ON notifications(recipient_user_id, created_at DESC);
CREATE INDEX idx_notifications_recipient_read ON notifications(recipient_user_id, is_read);
