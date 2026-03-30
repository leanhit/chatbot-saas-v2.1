-- Add package columns to tenants table
ALTER TABLE tenants 
ADD COLUMN IF NOT EXISTS current_package_id VARCHAR(50),
ADD COLUMN IF NOT EXISTS package_activated_at TIMESTAMP;

-- Create index for package_id
CREATE INDEX IF NOT EXISTS idx_tenants_current_package_id ON tenants(current_package_id);

-- Add comments
COMMENT ON COLUMN tenants.current_package_id IS 'Current package ID (free, pro, business, enterprise)';
COMMENT ON COLUMN tenants.package_activated_at IS 'When the current package was activated';
