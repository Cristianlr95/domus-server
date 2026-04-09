CREATE TABLE visits (
    id UUID PRIMARY KEY,
    visitor_name VARCHAR(150) NOT NULL,
    visitor_document VARCHAR(50) NOT NULL,
    visitor_phone VARCHAR(50),
    vehicle_plate VARCHAR(20),
    resident_user_id UUID,
    resident_name VARCHAR(150) NOT NULL,
    unit_label VARCHAR(80),
    block_label VARCHAR(80),
    entry_at TIMESTAMP WITH TIME ZONE,
    exit_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(30) NOT NULL,
    registration_type VARCHAR(40) NOT NULL,
    observations VARCHAR(500),
    recorded_by_user_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_visits_resident_user FOREIGN KEY (resident_user_id) REFERENCES users(id),
    CONSTRAINT fk_visits_recorded_by_user FOREIGN KEY (recorded_by_user_id) REFERENCES users(id)
);

CREATE INDEX idx_visits_status ON visits(status);
CREATE INDEX idx_visits_created_at ON visits(created_at DESC);
CREATE INDEX idx_visits_resident_user_id ON visits(resident_user_id);
CREATE INDEX idx_visits_recorded_by_user_id ON visits(recorded_by_user_id);
