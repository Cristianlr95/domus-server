CREATE TABLE properties (
    id UUID PRIMARY KEY,
    label VARCHAR(80) NOT NULL UNIQUE,
    block_label VARCHAR(80),
    type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    bedrooms INTEGER NOT NULL,
    bathrooms INTEGER NOT NULL,
    square_meters NUMERIC(10, 2),
    floor INTEGER,
    owner_name VARCHAR(150) NOT NULL,
    owner_email VARCHAR(150) NOT NULL,
    owner_phone VARCHAR(50),
    residents_count INTEGER,
    observations VARCHAR(500),
    image_url VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_properties_type ON properties(type);
CREATE INDEX idx_properties_status ON properties(status);
CREATE INDEX idx_properties_block_label ON properties(block_label);
CREATE INDEX idx_properties_owner_email ON properties(owner_email);

INSERT INTO permissions (id, code, description) VALUES
('c1000000-0000-0000-0000-000000000025', 'properties.read', 'View properties'),
('c1000000-0000-0000-0000-000000000026', 'properties.manage', 'Create and update properties');

INSERT INTO role_permissions (role_id, permission_id)
SELECT 'aa4f8752-3baa-46fb-934b-54cc2d9d1001', id
FROM permissions
WHERE code IN ('properties.read', 'properties.manage');

INSERT INTO properties (
    id,
    label,
    block_label,
    type,
    status,
    bedrooms,
    bathrooms,
    square_meters,
    floor,
    owner_name,
    owner_email,
    owner_phone,
    residents_count,
    observations,
    image_url,
    created_at,
    updated_at
) VALUES
(
    'd14f8752-3baa-46fb-934b-54cc2d9d4001',
    'Depto 804',
    'Torre A',
    'APARTAMENTO',
    'OCUPADA',
    3,
    2,
    82.50,
    8,
    'Rocio Residente',
    'residente@domus.cl',
    '+56911112222',
    2,
    'Unidad piloto vinculada a los datos base del proyecto.',
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
