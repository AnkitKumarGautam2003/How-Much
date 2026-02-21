CREATE TABLE loans (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version BIGINT NOT NULL,
    borrower_id UUID NOT NULL,
    principal_amount BIGINT NOT NULL,
    interest_rate_bps INTEGER NOT NULL,
    term_months INTEGER NOT NULL,
    funded_amount BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_loans_borrower_id_users FOREIGN KEY (borrower_id) REFERENCES users(id)
);

CREATE INDEX idx_loans_borrower_id ON loans (borrower_id);
