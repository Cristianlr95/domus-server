CREATE TABLE units (
    id UUID PRIMARY KEY,
    unit_code VARCHAR(50) NOT NULL,
    block_label VARCHAR(80) NOT NULL,
    floor_number INTEGER,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    observations VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_units_block_and_code UNIQUE (block_label, unit_code)
);

ALTER TABLE residents ADD COLUMN unit_id UUID;
ALTER TABLE residents ADD CONSTRAINT fk_residents_unit FOREIGN KEY (unit_id) REFERENCES units(id);

CREATE INDEX idx_units_active ON units(is_active);
CREATE INDEX idx_units_block_label ON units(block_label);
CREATE INDEX idx_residents_unit_id ON residents(unit_id);
