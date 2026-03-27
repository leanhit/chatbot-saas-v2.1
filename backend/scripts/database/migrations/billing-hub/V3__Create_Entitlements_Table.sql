-- Create updated_at function if not exists
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create Entitlements Table (READ-ONLY as per design)
CREATE TABLE IF NOT EXISTS entitlements (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    feature_name VARCHAR(255) NOT NULL,
    feature_type VARCHAR(50) NOT NULL, -- BOOLEAN, NUMERIC, STRING
    is_enabled BOOLEAN DEFAULT FALSE,
    numeric_limit DECIMAL(19,4),
    string_value TEXT,
    conditions JSONB,
    granted_by VARCHAR(100), -- SYSTEM, ADMIN, SUBSCRIPTION
    source_reference UUID, -- Reference to subscription or manual grant
    valid_from DATE,
    valid_until DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, feature_name)
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_entitlements_tenant_id ON entitlements(tenant_id);
CREATE INDEX IF NOT EXISTS idx_entitlements_feature_name ON entitlements(feature_name);
CREATE INDEX IF NOT EXISTS idx_entitlements_is_enabled ON entitlements(is_enabled);
CREATE INDEX IF NOT EXISTS idx_entitlements_valid_period ON entitlements(valid_from, valid_until);

-- Create trigger for updated_at
CREATE TRIGGER update_entitlements_updated_at 
    BEFORE UPDATE ON entitlements 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
