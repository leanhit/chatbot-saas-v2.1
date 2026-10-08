-- ============================================================
-- V12: Create webhooks table for webhook management
-- ============================================================

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

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_webhooks_status ON webhooks(status);
CREATE INDEX IF NOT EXISTS idx_webhooks_next_retry ON webhooks(next_retry_at);
CREATE INDEX IF NOT EXISTS idx_webhooks_is_active ON webhooks(is_active);

-- Add comments
COMMENT ON TABLE webhooks IS 'Webhook configuration for payment events';
COMMENT ON COLUMN webhooks.url IS 'Webhook endpoint URL';
COMMENT ON COLUMN webhooks.secret IS 'Webhook secret for signature verification';
COMMENT ON COLUMN webhooks.status IS 'Webhook status (ACTIVE, FAILED, DISABLED)';
