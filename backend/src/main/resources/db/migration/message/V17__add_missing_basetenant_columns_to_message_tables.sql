-- ============================================================
-- V17: Add missing BaseTenantEntity columns to message tables
-- ============================================================

ALTER TABLE conversations ADD COLUMN IF NOT EXISTS tenant_key VARCHAR(255);
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE agents ADD COLUMN IF NOT EXISTS tenant_key VARCHAR(255);
ALTER TABLE agents ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE agents ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
ALTER TABLE agents ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE skills ADD COLUMN IF NOT EXISTS tenant_key VARCHAR(255);
ALTER TABLE skills ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE skills ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
ALTER TABLE skills ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE routing_rules ADD COLUMN IF NOT EXISTS tenant_key VARCHAR(255);
ALTER TABLE routing_rules ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE routing_rules ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
ALTER TABLE routing_rules ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE sla_configurations ADD COLUMN IF NOT EXISTS tenant_key VARCHAR(255);
ALTER TABLE sla_configurations ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE sla_configurations ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
ALTER TABLE sla_configurations ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE escalation_tiers ADD COLUMN IF NOT EXISTS tenant_key VARCHAR(255);
ALTER TABLE escalation_tiers ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE escalation_tiers ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
ALTER TABLE escalation_tiers ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS auto_assign_configs (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    interval_seconds INTEGER NOT NULL DEFAULT 30,
    max_concurrent_per_agent INTEGER NOT NULL DEFAULT 5,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tenant_key VARCHAR(255),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_auto_assign_tenant ON auto_assign_configs(tenant_id);
