-- V8: Per-tenant VIP tier thresholds for customer segmentation
CREATE TABLE IF NOT EXISTS vip_configs (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    tenant_key VARCHAR(255),
    basic_min_conversations INTEGER DEFAULT 3,
    premium_min_conversations INTEGER DEFAULT 10,
    premium_min_satisfaction DOUBLE PRECISION DEFAULT 4.0,
    enterprise_min_conversations INTEGER DEFAULT 50,
    auto_recalculate_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    description TEXT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_vip_config_tenant ON vip_configs(tenant_id);
CREATE INDEX IF NOT EXISTS idx_vip_config_tenant ON vip_configs(tenant_id);
