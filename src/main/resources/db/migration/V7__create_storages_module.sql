CREATE TABLE storages (
    id UUID PRIMARY KEY,
    storage_code VARCHAR(50) NOT NULL UNIQUE,
    storage_type VARCHAR(30) NOT NULL,
    occupancy_status VARCHAR(30) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    unit_id UUID NOT NULL,
    observations VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_storages_unit FOREIGN KEY (unit_id) REFERENCES units(id)
);

CREATE INDEX idx_storages_active ON storages(is_active);
CREATE INDEX idx_storages_occupancy_status ON storages(occupancy_status);
CREATE INDEX idx_storages_unit_id ON storages(unit_id);
