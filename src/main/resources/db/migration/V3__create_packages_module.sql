CREATE TABLE packages (
    id UUID PRIMARY KEY,
    description VARCHAR(180) NOT NULL,
    sender_name VARCHAR(150),
    package_type VARCHAR(40) NOT NULL,
    resident_user_id UUID,
    resident_name VARCHAR(150) NOT NULL,
    unit_label VARCHAR(80),
    block_label VARCHAR(80),
    received_at TIMESTAMP WITH TIME ZONE NOT NULL,
    delivered_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(30) NOT NULL,
    observations VARCHAR(500),
    received_by_name VARCHAR(150),
    delivered_to_name VARCHAR(150),
    recorded_by_user_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_packages_resident_user FOREIGN KEY (resident_user_id) REFERENCES users(id),
    CONSTRAINT fk_packages_recorded_by_user FOREIGN KEY (recorded_by_user_id) REFERENCES users(id)
);

CREATE INDEX idx_packages_status ON packages(status);
CREATE INDEX idx_packages_received_at ON packages(received_at DESC);
CREATE INDEX idx_packages_resident_user_id ON packages(resident_user_id);
CREATE INDEX idx_packages_recorded_by_user_id ON packages(recorded_by_user_id);
