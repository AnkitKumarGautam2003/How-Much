CREATE TABLE users (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(60) NOT NULL,
    role VARCHAR(20) NOT NULL,
    kyc_status VARCHAR(20) NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE idempotency_records (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    request_hash VARCHAR(128) NOT NULL,
    response_status INTEGER NOT NULL,
    response_body TEXT NOT NULL,
    CONSTRAINT uk_idempotency_records_idempotency_key UNIQUE (idempotency_key)
);

CREATE INDEX idx_idempotency_key ON idempotency_records (idempotency_key);
