-- ============================================================
-- V2: Create runtime_configs table for runtime configuration management
-- ============================================================

CREATE TABLE IF NOT EXISTS runtime_configs (
    id BIGSERIAL PRIMARY KEY,
    tenant_key VARCHAR(255),
    tenant_id BIGINT NOT NULL,
    config_key VARCHAR(200) UNIQUE NOT NULL,
    config_value TEXT,
    default_value TEXT,
    config_type VARCHAR(50) NOT NULL,
    config_scope VARCHAR(50) NOT NULL,
    user_id BIGINT,
    is_encrypted BOOLEAN NOT NULL DEFAULT FALSE,
    is_readonly BOOLEAN NOT NULL DEFAULT FALSE,
    description VARCHAR(500),
    version INTEGER NOT NULL DEFAULT 1,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_runtime_configs_tenant ON runtime_configs(tenant_id);
CREATE INDEX IF NOT EXISTS idx_runtime_configs_key ON runtime_configs(config_key);
CREATE INDEX IF NOT EXISTS idx_runtime_configs_type ON runtime_configs(config_type);
CREATE INDEX IF NOT EXISTS idx_runtime_configs_scope ON runtime_configs(config_scope);
CREATE INDEX IF NOT EXISTS idx_runtime_configs_user ON runtime_configs(user_id);

-- Add comments
COMMENT ON TABLE runtime_configs IS 'Runtime configuration storage for tenant-specific settings';
COMMENT ON COLUMN runtime_configs.config_key IS 'Unique configuration key';
COMMENT ON COLUMN runtime_configs.config_value IS 'Configuration value (may be encrypted)';
COMMENT ON COLUMN runtime_configs.config_type IS 'Configuration type (STRING, INTEGER, BOOLEAN, JSON)';
COMMENT ON COLUMN runtime_configs.config_scope IS 'Configuration scope (SYSTEM, TENANT, USER)';
COMMENT ON COLUMN runtime_configs.version IS 'Configuration version for optimistic locking';
