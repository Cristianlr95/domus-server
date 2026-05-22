CREATE TABLE common_spaces (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    type VARCHAR(30) NOT NULL,
    capacity INTEGER NOT NULL,
    description VARCHAR(500),
    image_url VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE bookings (
    id UUID PRIMARY KEY,
    common_space_id UUID NOT NULL,
    resident_user_id UUID NOT NULL,
    approved_by_user_id UUID,
    status VARCHAR(30) NOT NULL,
    booking_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    guest_count INTEGER,
    observations VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_bookings_common_space FOREIGN KEY (common_space_id) REFERENCES common_spaces(id),
    CONSTRAINT fk_bookings_resident_user FOREIGN KEY (resident_user_id) REFERENCES users(id),
    CONSTRAINT fk_bookings_approved_by_user FOREIGN KEY (approved_by_user_id) REFERENCES users(id)
);

CREATE INDEX idx_common_spaces_active ON common_spaces(is_active);
CREATE INDEX idx_common_spaces_type ON common_spaces(type);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_date ON bookings(booking_date);
CREATE INDEX idx_bookings_common_space_date ON bookings(common_space_id, booking_date);
CREATE INDEX idx_bookings_resident_user ON bookings(resident_user_id);

INSERT INTO permissions (id, code, description) VALUES
('c1000000-0000-0000-0000-000000000027', 'bookings.read', 'View common space bookings'),
('c1000000-0000-0000-0000-000000000028', 'bookings.create', 'Create common space bookings'),
('c1000000-0000-0000-0000-000000000029', 'bookings.update', 'Update common space bookings');

INSERT INTO role_permissions (role_id, permission_id)
SELECT 'aa4f8752-3baa-46fb-934b-54cc2d9d1001', id
FROM permissions
WHERE code IN ('bookings.read', 'bookings.create', 'bookings.update');

INSERT INTO role_permissions (role_id, permission_id)
SELECT 'aa4f8752-3baa-46fb-934b-54cc2d9d1002', id
FROM permissions
WHERE code IN ('bookings.read', 'bookings.update');

INSERT INTO role_permissions (role_id, permission_id)
SELECT 'aa4f8752-3baa-46fb-934b-54cc2d9d1003', id
FROM permissions
WHERE code IN ('bookings.read', 'bookings.create');

INSERT INTO common_spaces (
    id,
    name,
    type,
    capacity,
    description,
    image_url,
    is_active,
    created_at,
    updated_at
) VALUES
(
    'e14f8752-3baa-46fb-934b-54cc2d9e5001',
    'Salon comunal',
    'SALON_COMUNAL',
    80,
    'Espacio comunitario para reuniones y celebraciones.',
    NULL,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    'e14f8752-3baa-46fb-934b-54cc2d9e5002',
    'Terraza quincho',
    'TERRAZA',
    35,
    'Terraza compartida para actividades de residentes.',
    NULL,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    'e14f8752-3baa-46fb-934b-54cc2d9e5003',
    'Gimnasio',
    'GIMNASIO',
    12,
    'Gimnasio del edificio con cupos limitados por bloque horario.',
    NULL,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
