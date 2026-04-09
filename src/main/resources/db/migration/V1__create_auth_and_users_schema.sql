CREATE TABLE roles (
    id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

INSERT INTO roles (id, name) VALUES
('aa4f8752-3baa-46fb-934b-54cc2d9d1001', 'ADMIN'),
('aa4f8752-3baa-46fb-934b-54cc2d9d1002', 'CONSERJERIA'),
('aa4f8752-3baa-46fb-934b-54cc2d9d1003', 'RESIDENTE');

INSERT INTO users (id, first_name, last_name, email, password_hash, is_active, created_at, updated_at) VALUES
('bb4f8752-3baa-46fb-934b-54cc2d9d2001', 'Admin', 'Domus', 'admin@domus.cl', '$2a$10$Az18t3X0kYyMolU3CRPF4uKFOj/3aqTyQhsaKKdx5Haf0cAMJCwlm', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('bb4f8752-3baa-46fb-934b-54cc2d9d2002', 'Ana', 'Porteria', 'conserjeria@domus.cl', '$2a$10$Az18t3X0kYyMolU3CRPF4uKFOj/3aqTyQhsaKKdx5Haf0cAMJCwlm', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('bb4f8752-3baa-46fb-934b-54cc2d9d2003', 'Rocio', 'Residente', 'residente@domus.cl', '$2a$10$Az18t3X0kYyMolU3CRPF4uKFOj/3aqTyQhsaKKdx5Haf0cAMJCwlm', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_roles (user_id, role_id) VALUES
('bb4f8752-3baa-46fb-934b-54cc2d9d2001', 'aa4f8752-3baa-46fb-934b-54cc2d9d1001'),
('bb4f8752-3baa-46fb-934b-54cc2d9d2002', 'aa4f8752-3baa-46fb-934b-54cc2d9d1002'),
('bb4f8752-3baa-46fb-934b-54cc2d9d2003', 'aa4f8752-3baa-46fb-934b-54cc2d9d1003');
