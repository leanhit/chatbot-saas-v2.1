-- ============================================================
-- V13: Create webhook_events table for webhook event types
-- ============================================================

CREATE TABLE IF NOT EXISTS webhook_events (
    webhook_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    PRIMARY KEY (webhook_id, event_type),
    CONSTRAINT fk_webhook_events_webhook FOREIGN KEY (webhook_id) REFERENCES webhooks(id) ON DELETE CASCADE
);

-- Create index for performance
CREATE INDEX IF NOT EXISTS idx_webhook_events_type ON webhook_events(event_type);

-- Add comments
COMMENT ON TABLE webhook_events IS 'Webhook event types collection';
COMMENT ON COLUMN webhook_events.event_type IS 'Event type (PAYMENT_CREATED, PAYMENT_COMPLETED, etc.)';
