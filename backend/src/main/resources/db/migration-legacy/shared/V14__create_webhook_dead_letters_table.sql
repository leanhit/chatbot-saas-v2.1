-- ============================================================
-- V14: Create webhook_dead_letters table for failed webhooks
-- ============================================================

CREATE TABLE IF NOT EXISTS webhook_dead_letters (
    id BIGSERIAL PRIMARY KEY,
    webhook_id BIGINT NOT NULL,
    webhook_name VARCHAR(100) NOT NULL,
    webhook_url VARCHAR(500) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    payment_reference_code VARCHAR(100) NOT NULL,
    retry_attempts INTEGER NOT NULL,
    payload TEXT NOT NULL,
    last_error TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    processed_at TIMESTAMP,
    processed_by VARCHAR(255),
    processing_notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_webhook_dead_letters_status ON webhook_dead_letters(status);
CREATE INDEX IF NOT EXISTS idx_webhook_dead_letters_webhook ON webhook_dead_letters(webhook_id);
CREATE INDEX IF NOT EXISTS idx_webhook_dead_letters_payment ON webhook_dead_letters(payment_reference_code);

-- Add comments
COMMENT ON TABLE webhook_dead_letters IS 'Dead letter queue for failed webhooks';
COMMENT ON COLUMN webhook_dead_letters.status IS 'Processing status (PENDING, PROCESSED, DISCARDED)';
