ALTER TABLE loans
    ADD COLUMN lender_id UUID,
    ADD COLUMN funded_at TIMESTAMPTZ;

ALTER TABLE loans
    ADD CONSTRAINT fk_loans_lender_id_users FOREIGN KEY (lender_id) REFERENCES users(id);

CREATE INDEX idx_loans_lender_id ON loans (lender_id);
