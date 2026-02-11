-- Add tenantKey column to tenants table
-- This migration adds the tenantKey UUID column to support the new tenantKey architecture

ALTER TABLE tenants ADD COLUMN tenant_key VARCHAR(255) UNIQUE NOT NULL;

-- Generate UUID for existing tenants
UPDATE tenants SET tenant_key = CONCAT(
    SUBSTRING(REPLACE(UUID(), '-', ''), 1, 8), '-',
    SUBSTRING(REPLACE(UUID(), '-', ''), 9, 4), '-',
    SUBSTRING(REPLACE(UUID(), '-', ''), 13, 4), '-',
    SUBSTRING(REPLACE(UUID(), '-', ''), 17, 12)
) WHERE tenant_key IS NULL;

-- Add index for faster lookups
CREATE INDEX idx_tenants_tenant_key ON tenants(tenant_key);

-- Add comment
COMMENT ON COLUMN tenants.tenant_key IS 'Unique UUID identifier for frontend use';
