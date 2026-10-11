-- ============================================================
-- V5: Add missing BaseTenantEntity columns to facebook_connection table
-- ============================================================

ALTER TABLE facebook_connection 
ADD COLUMN IF NOT EXISTS tenant_key VARCHAR(255),
ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_facebook_connection_tenant_key ON facebook_connection(tenant_key);
CREATE INDEX IF NOT EXISTS idx_facebook_connection_is_deleted ON facebook_connection(is_deleted);
