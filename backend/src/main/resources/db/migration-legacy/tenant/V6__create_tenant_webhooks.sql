-- Tenant outbound webhooks for conversation events (Phase 6)
CREATE TABLE IF NOT EXISTS tenant_webhooks (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    url VARCHAR(2048) NOT NULL,
    secret VARCHAR(255),
    events TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_tenant_webhooks_tenant_active
    ON tenant_webhooks(tenant_id) WHERE is_active = true;
