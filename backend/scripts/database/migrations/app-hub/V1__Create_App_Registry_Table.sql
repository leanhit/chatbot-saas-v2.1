-- Create updated_at function if not exists
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create App Registry Table
CREATE TABLE IF NOT EXISTS app_registry (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    app_type VARCHAR(50) DEFAULT 'INTEGRATION',
    status VARCHAR(50) DEFAULT 'ACTIVE',
    version VARCHAR(50),
    developer VARCHAR(255),
    icon_url TEXT,
    screenshots JSONB,
    features JSONB,
    requirements JSONB,
    pricing JSONB,
    configuration_schema JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_app_registry_category ON app_registry(category);
CREATE INDEX IF NOT EXISTS idx_app_registry_status ON app_registry(status);
CREATE INDEX IF NOT EXISTS idx_app_registry_type ON app_registry(app_type);

-- Create trigger for updated_at
CREATE TRIGGER update_app_registry_updated_at 
    BEFORE UPDATE ON app_registry 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
