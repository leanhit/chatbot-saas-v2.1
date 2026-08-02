-- ============================================================
-- V0: Create environment_configs table (Base table)
-- ============================================================

CREATE TABLE IF NOT EXISTS environment_configs (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    environment VARCHAR(50) NOT NULL,
    config_key VARCHAR(200) NOT NULL,
    config_value TEXT,
    is_encrypted BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    description VARCHAR(500),
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_environment_configs_tenant ON environment_configs(tenant_id);
CREATE INDEX IF NOT EXISTS idx_environment_configs_env ON environment_configs(environment);
CREATE INDEX IF NOT EXISTS idx_environment_configs_key ON environment_configs(config_key);
CREATE INDEX IF NOT EXISTS idx_environment_configs_active ON environment_configs(is_active);
