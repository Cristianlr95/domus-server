CREATE TABLE parking_spots (
    id UUID PRIMARY KEY,
    spot_code VARCHAR(50) NOT NULL UNIQUE,
    parking_type VARCHAR(30) NOT NULL,
    occupancy_status VARCHAR(30) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    unit_id UUID,
    resident_id UUID,
    vehicle_plate VARCHAR(20),
    observations VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_parking_unit FOREIGN KEY (unit_id) REFERENCES units(id),
    CONSTRAINT fk_parking_resident FOREIGN KEY (resident_id) REFERENCES residents(id)
);

CREATE INDEX idx_parking_active ON parking_spots(is_active);
CREATE INDEX idx_parking_occupancy_status ON parking_spots(occupancy_status);
CREATE INDEX idx_parking_type ON parking_spots(parking_type);
CREATE INDEX idx_parking_unit_id ON parking_spots(unit_id);
CREATE INDEX idx_parking_resident_id ON parking_spots(resident_id);
