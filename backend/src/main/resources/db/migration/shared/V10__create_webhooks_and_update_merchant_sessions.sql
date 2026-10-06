-- V10: Create webhooks tables and align merchant_payment_sessions table

CREATE TABLE IF NOT EXISTS webhooks (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    url VARCHAR(500) UNIQUE NOT NULL,
    secret VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    retry_count INTEGER NOT NULL DEFAULT 3,
    current_retry_attempt INTEGER NOT NULL DEFAULT 0,
    timeout_seconds INTEGER NOT NULL DEFAULT 10,
    next_retry_at TIMESTAMP,
    last_error TEXT,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    last_triggered_at TIMESTAMP,
    success_count INTEGER DEFAULT 0,
    failure_count INTEGER DEFAULT 0,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS webhook_events (
    webhook_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    CONSTRAINT fk_webhook_events_webhook FOREIGN KEY (webhook_id) REFERENCES webhooks(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_webhooks_url ON webhooks(url);
CREATE INDEX IF NOT EXISTS idx_webhooks_status ON webhooks(status);

ALTER TABLE merchant_payment_sessions 
    ADD COLUMN IF NOT EXISTS tenant_id BIGINT,
    ADD COLUMN IF NOT EXISTS metadata TEXT,
    ADD COLUMN IF NOT EXISTS payment_reference_code VARCHAR(100),
    ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS cancelled_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS failed_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS failure_reason VARCHAR(500),
    ADD COLUMN IF NOT EXISTS webhook_sent_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS webhook_status VARCHAR(50),
    ADD COLUMN IF NOT EXISTS webhook_retry_count INTEGER DEFAULT 0;
