-- Webhook delivery log + retry queue (Phase 6)
CREATE TABLE IF NOT EXISTS tenant_webhook_deliveries (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    webhook_id BIGINT NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    conversation_id BIGINT,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempts INT NOT NULL DEFAULT 0,
    next_retry_at TIMESTAMP NOT NULL DEFAULT now(),
    last_error TEXT,
    delivered_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_tenant_webhook_deliveries_retry
    ON tenant_webhook_deliveries(status, next_retry_at) WHERE status = 'PENDING';
CREATE INDEX IF NOT EXISTS idx_tenant_webhook_deliveries_tenant
    ON tenant_webhook_deliveries(tenant_id);
