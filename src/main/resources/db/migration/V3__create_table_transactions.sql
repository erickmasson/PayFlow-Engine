CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    payer_wallet_id BIGINT NOT NULL REFERENCES wallets(id),
    payee_wallet_id BIGINT NOT NULL REFERENCES wallets(id),
    amount NUMERIC(15, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    idempotency_key VARCHAR(100) UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_positive_amount CHECK (amount > 0),
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'CANCELLED'))
);