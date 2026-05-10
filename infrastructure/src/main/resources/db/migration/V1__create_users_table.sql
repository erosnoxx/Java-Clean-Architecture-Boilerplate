CREATE TABLE users (
    id UUID PRIMARY KEY,

    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(128) NOT NULL,

    role VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,

    CONSTRAINT uq_users_email
       UNIQUE (email),

    CONSTRAINT chk_users_name_length
       CHECK (LENGTH(TRIM(name)) BETWEEN 2 AND 100),

    CONSTRAINT chk_users_role
       CHECK (role IN ('ADMIN', 'USER'))
);

CREATE INDEX idx_users_email
    ON users(email);