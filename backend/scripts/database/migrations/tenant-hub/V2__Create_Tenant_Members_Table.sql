-- Create Tenant Members Table
CREATE TABLE IF NOT EXISTS tenant_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('OWNER', 'ADMIN', 'MEMBER', 'VIEWER')),
    status VARCHAR(50) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'INVITED', 'SUSPENDED')),
    invited_by UUID,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, user_id)
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_members_tenant_id ON tenant_members(tenant_id);
CREATE INDEX IF NOT EXISTS idx_members_user_id ON tenant_members(user_id);
CREATE INDEX IF NOT EXISTS idx_members_role ON tenant_members(role);
CREATE INDEX IF NOT EXISTS idx_members_status ON tenant_members(status);
CREATE INDEX IF NOT EXISTS idx_members_joined_at ON tenant_members(joined_at);

-- Create composite index for active members
CREATE INDEX IF NOT EXISTS idx_members_active ON tenant_members(tenant_id, status) 
WHERE status = 'ACTIVE';

-- Create trigger for updated_at
CREATE OR REPLACE FUNCTION update_tenant_members_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_tenant_members_updated_at 
    BEFORE UPDATE ON tenant_members 
    FOR EACH ROW 
    EXECUTE FUNCTION update_tenant_members_updated_at_column();

-- Add foreign key constraints (assuming users table exists in identity database)
-- These would be handled at the application level due to cross-database references
