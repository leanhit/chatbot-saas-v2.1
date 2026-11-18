-- Tenant API keys for programmatic access (Phase 6)
CREATE TABLE IF NOT EXISTS tenant_api_keys (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    key_prefix VARCHAR(20) NOT NULL,
    key_hash VARCHAR(128) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    is_active BOOLEAN NOT NULL DEFAULT true,
    last_used_at TIMESTAMP,
    usage_count BIGINT NOT NULL DEFAULT 0,
    expires_at TIMESTAMP,
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_tenant_api_keys_hash ON tenant_api_keys(key_hash);
CREATE INDEX IF NOT EXISTS idx_tenant_api_keys_tenant ON tenant_api_keys(tenant_id);
