CREATE TABLE residents (
    id UUID PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    document_number VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(150) UNIQUE,
    phone VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    resident_type VARCHAR(30) NOT NULL,
    unit_label VARCHAR(80),
    block_label VARCHAR(80),
    linked_user_id UUID UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_residents_linked_user FOREIGN KEY (linked_user_id) REFERENCES users(id)
);

CREATE INDEX idx_residents_active ON residents(is_active);
CREATE INDEX idx_residents_last_name ON residents(last_name);
CREATE INDEX idx_residents_unit_label ON residents(unit_label);
