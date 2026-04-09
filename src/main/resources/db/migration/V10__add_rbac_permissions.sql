CREATE TABLE permissions (
    id UUID PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL
);

CREATE TABLE role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id),
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(id)
);

CREATE INDEX idx_role_permissions_permission_id ON role_permissions(permission_id);

INSERT INTO permissions (id, code, description) VALUES
('c1000000-0000-0000-0000-000000000001', 'visits.read', 'View visits'),
('c1000000-0000-0000-0000-000000000002', 'visits.create', 'Create visits'),
('c1000000-0000-0000-0000-000000000003', 'visits.update', 'Update visits'),
('c1000000-0000-0000-0000-000000000004', 'packages.read', 'View packages'),
('c1000000-0000-0000-0000-000000000005', 'packages.create', 'Create packages'),
('c1000000-0000-0000-0000-000000000006', 'packages.update', 'Update packages'),
('c1000000-0000-0000-0000-000000000007', 'residents.read', 'View residents'),
('c1000000-0000-0000-0000-000000000008', 'residents.manage', 'Create and update residents'),
('c1000000-0000-0000-0000-000000000009', 'units.read', 'View units'),
('c1000000-0000-0000-0000-000000000010', 'units.manage', 'Create and update units'),
('c1000000-0000-0000-0000-000000000011', 'parking.read', 'View parking spots'),
('c1000000-0000-0000-0000-000000000012', 'parking.manage', 'Create and update parking spots'),
('c1000000-0000-0000-0000-000000000013', 'storages.read', 'View storages'),
('c1000000-0000-0000-0000-000000000014', 'storages.manage', 'Create and update storages'),
('c1000000-0000-0000-0000-000000000015', 'messaging.read', 'Read conversations and messages'),
('c1000000-0000-0000-0000-000000000016', 'messaging.create', 'Send internal messages'),
('c1000000-0000-0000-0000-000000000017', 'notifications.read', 'Read notifications'),
('c1000000-0000-0000-0000-000000000018', 'concierge.dashboard.read', 'View concierge dashboard'),
('c1000000-0000-0000-0000-000000000019', 'users.read', 'View users'),
('c1000000-0000-0000-0000-000000000020', 'users.manage', 'Manage users'),
('c1000000-0000-0000-0000-000000000021', 'roles.read', 'View roles'),
('c1000000-0000-0000-0000-000000000022', 'permissions.read', 'View permissions'),
('c1000000-0000-0000-0000-000000000023', 'admin.dashboard.read', 'View admin dashboard'),
('c1000000-0000-0000-0000-000000000024', 'audit.read', 'View audit data');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ADMIN';

INSERT INTO role_permissions (role_id, permission_id)
SELECT 'aa4f8752-3baa-46fb-934b-54cc2d9d1002', id
FROM permissions
WHERE code IN (
    'visits.read',
    'visits.create',
    'visits.update',
    'packages.read',
    'packages.create',
    'packages.update',
    'residents.read',
    'residents.manage',
    'units.read',
    'units.manage',
    'parking.read',
    'parking.manage',
    'storages.read',
    'storages.manage',
    'messaging.read',
    'messaging.create',
    'notifications.read',
    'concierge.dashboard.read'
);

INSERT INTO role_permissions (role_id, permission_id)
SELECT 'aa4f8752-3baa-46fb-934b-54cc2d9d1003', id
FROM permissions
WHERE code IN (
    'messaging.read',
    'messaging.create',
    'notifications.read'
);
