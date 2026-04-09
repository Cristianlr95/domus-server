CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100),
    action VARCHAR(50) NOT NULL,
    summary VARCHAR(500) NOT NULL,
    actor_user_id UUID,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    previous_data TEXT,
    new_data TEXT,
    context_data TEXT,
    CONSTRAINT fk_audit_logs_actor_user FOREIGN KEY (actor_user_id) REFERENCES users(id)
);

CREATE INDEX idx_audit_logs_entity_type ON audit_logs(entity_type);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_actor_user_id ON audit_logs(actor_user_id);
CREATE INDEX idx_audit_logs_occurred_at ON audit_logs(occurred_at DESC);
