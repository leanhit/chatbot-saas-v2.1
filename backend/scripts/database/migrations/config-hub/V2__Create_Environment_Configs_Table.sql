-- Create updated_at function if not exists
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create Environment Configs Table
CREATE TABLE IF NOT EXISTS environment_configs (
    id BIGSERIAL PRIMARY KEY,
    environment VARCHAR(50) NOT NULL,
    config_key VARCHAR(255) NOT NULL,
    config_value TEXT,
    config_type VARCHAR(50) DEFAULT 'STRING',
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(environment, config_key)
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_env_configs_environment ON environment_configs(environment);
CREATE INDEX IF NOT EXISTS idx_env_configs_key ON environment_configs(config_key);

-- Create trigger for updated_at
CREATE TRIGGER update_environment_configs_updated_at 
    BEFORE UPDATE ON environment_configs 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
