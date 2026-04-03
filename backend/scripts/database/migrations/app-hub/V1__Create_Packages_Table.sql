-- Create packages table for dynamic package management
CREATE TABLE IF NOT EXISTS packages (
    id BIGSERIAL PRIMARY KEY,
    package_id VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(10) NOT NULL DEFAULT 'VND',
    duration VARCHAR(50) NOT NULL,
    description TEXT,
    message_limit INTEGER NOT NULL DEFAULT 0,
    chatbot_limit INTEGER NOT NULL DEFAULT 1,
    connection_limit INTEGER NOT NULL DEFAULT 1,
    has_priority_support BOOLEAN NOT NULL DEFAULT FALSE,
    has_analytics BOOLEAN NOT NULL DEFAULT FALSE,
    has_advanced_analytics BOOLEAN NOT NULL DEFAULT FALSE,
    has_custom_integrations BOOLEAN NOT NULL DEFAULT FALSE,
    has_dedicated_support BOOLEAN NOT NULL DEFAULT FALSE,
    has_custom_features BOOLEAN NOT NULL DEFAULT FALSE,
    has_sla_guarantee BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    badge VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_packages_package_id ON packages(package_id);
CREATE INDEX IF NOT EXISTS idx_packages_is_active ON packages(is_active);
CREATE INDEX IF NOT EXISTS idx_packages_sort_order ON packages(sort_order);

-- Create trigger to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_packages_updated_at 
    BEFORE UPDATE ON packages 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
