-- ============================================================
-- V2: Add missing columns to environment_configs table for BaseTenantEntity compatibility
-- ============================================================

ALTER TABLE environment_configs ADD COLUMN IF NOT EXISTS tenant_key VARCHAR(255);
ALTER TABLE environment_configs ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
ALTER TABLE environment_configs ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
