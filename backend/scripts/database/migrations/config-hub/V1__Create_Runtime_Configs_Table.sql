-- Create updated_at function if not exists
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create Runtime Configs Table
CREATE TABLE IF NOT EXISTS runtime_configs (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(255) UNIQUE NOT NULL,
    config_value TEXT,
    config_type VARCHAR(50) DEFAULT 'STRING',
    scope VARCHAR(50) NOT NULL, -- GLOBAL, TENANT, USER
    tenant_id BIGINT,
    user_id BIGINT,
    category VARCHAR(100),
    description TEXT,
    is_encrypted BOOLEAN DEFAULT FALSE,
    validation_rules JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_configs_key ON runtime_configs(config_key);
CREATE INDEX IF NOT EXISTS idx_configs_scope ON runtime_configs(scope);
CREATE INDEX IF NOT EXISTS idx_configs_tenant_id ON runtime_configs(tenant_id);
CREATE INDEX IF NOT EXISTS idx_configs_category ON runtime_configs(category);

-- Create trigger for updated_at
CREATE TRIGGER update_runtime_configs_updated_at 
    BEFORE UPDATE ON runtime_configs 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
