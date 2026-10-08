-- ============================================================
-- V17: Create auto_assign_configs table for auto-assignment configuration
-- ============================================================

CREATE TABLE IF NOT EXISTS auto_assign_configs (
    id BIGSERIAL PRIMARY KEY,
    tenant_key VARCHAR(255),
    tenant_id BIGINT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    interval_seconds INTEGER NOT NULL DEFAULT 30,
    max_concurrent_per_agent INTEGER NOT NULL DEFAULT 5,
    description TEXT,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_auto_assign_tenant ON auto_assign_configs(tenant_id);
CREATE INDEX IF NOT EXISTS idx_auto_assign_enabled ON auto_assign_configs(enabled);

-- Add comments
COMMENT ON TABLE auto_assign_configs IS 'Auto-assignment configuration for tenant-specific agent assignment';
COMMENT ON COLUMN auto_assign_configs.enabled IS 'Whether auto-assignment is enabled';
COMMENT ON COLUMN auto_assign_configs.interval_seconds IS 'Auto-assignment interval in seconds';
COMMENT ON COLUMN auto_assign_configs.max_concurrent_per_agent IS 'Maximum concurrent conversations per agent';
