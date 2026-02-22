-- Create Tenants Table
CREATE TABLE IF NOT EXISTS tenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'DELETED')),
    visibility VARCHAR(50) DEFAULT 'PRIVATE' CHECK (visibility IN ('PUBLIC', 'PRIVATE', 'INVITE_ONLY')),
    owner_id UUID NOT NULL,
    industry VARCHAR(100),
    company_size VARCHAR(50) CHECK (company_size IN ('SMALL', 'MEDIUM', 'LARGE', 'ENTERPRISE')),
    website TEXT,
    logo_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_tenants_owner_id ON tenants(owner_id);
CREATE INDEX IF NOT EXISTS idx_tenants_status ON tenants(status);
CREATE INDEX IF NOT EXISTS idx_tenants_visibility ON tenants(visibility);
CREATE INDEX IF NOT EXISTS idx_tenants_industry ON tenants(industry);
CREATE INDEX IF NOT EXISTS idx_tenants_company_size ON tenants(company_size);
CREATE INDEX IF NOT EXISTS idx_tenants_created_at ON tenants(created_at);

-- Create trigger for updated_at
CREATE OR REPLACE FUNCTION update_tenants_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_tenants_updated_at 
    BEFORE UPDATE ON tenants 
    FOR EACH ROW 
    EXECUTE FUNCTION update_tenants_updated_at_column();
