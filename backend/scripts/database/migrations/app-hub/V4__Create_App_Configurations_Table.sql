-- Create updated_at function if not exists
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create App Configurations Table
CREATE TABLE IF NOT EXISTS app_configurations (
    id BIGSERIAL PRIMARY KEY,
    app_id BIGINT NOT NULL REFERENCES app_registry(id) ON DELETE CASCADE,
    config_key VARCHAR(255) NOT NULL,
    config_value TEXT,
    config_type VARCHAR(50) NOT NULL,
    is_required BOOLEAN DEFAULT FALSE,
    is_encrypted BOOLEAN DEFAULT FALSE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(app_id, config_key)
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_app_configurations_app_id ON app_configurations(app_id);
CREATE INDEX IF NOT EXISTS idx_app_configurations_key ON app_configurations(config_key);

-- Create trigger for updated_at
CREATE TRIGGER update_app_configurations_updated_at 
    BEFORE UPDATE ON app_configurations 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
