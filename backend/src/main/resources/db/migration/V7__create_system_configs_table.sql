-- Create system_configs table for runtime configuration management
CREATE TABLE IF NOT EXISTS system_configs (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT NOT NULL,
    config_category VARCHAR(50) NOT NULL DEFAULT 'GENERAL',
    config_type VARCHAR(20) NOT NULL DEFAULT 'STRING',
    description TEXT,
    is_sensitive BOOLEAN NOT NULL DEFAULT FALSE,
    is_encrypted BOOLEAN NOT NULL DEFAULT FALSE,
    updated_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT chk_config_type CHECK (config_type IN ('STRING', 'INTEGER', 'BOOLEAN', 'JSON'))
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_system_configs_key ON system_configs(config_key);
CREATE INDEX IF NOT EXISTS idx_system_configs_category ON system_configs(config_category);
CREATE INDEX IF NOT EXISTS idx_system_configs_sensitive ON system_configs(is_sensitive);

-- Add comment to table
COMMENT ON TABLE system_configs IS 'System configuration storage for runtime settings including bank API configuration';
COMMENT ON COLUMN system_configs.config_key IS 'Unique configuration key (e.g., payment.bank-api.provider)';
COMMENT ON COLUMN system_configs.config_value IS 'Configuration value (may be encrypted if is_encrypted=true)';
COMMENT ON COLUMN system_configs.config_category IS 'Configuration category for grouping (e.g., BANK, WEBHOOK, EMAIL)';
COMMENT ON COLUMN system_configs.config_type IS 'Data type of the configuration value';
COMMENT ON COLUMN system_configs.is_sensitive IS 'Whether this config contains sensitive data (API keys, secrets)';
COMMENT ON COLUMN system_configs.is_encrypted IS 'Whether the config value is encrypted in storage';
COMMENT ON COLUMN system_configs.updated_by IS 'Username of the admin who last updated this config';

-- Initialize default bank configuration
INSERT INTO system_configs (config_key, config_value, config_category, config_type, description, is_sensitive, is_encrypted) VALUES
('payment.bank.name', 'Vietcombank', 'BANK', 'STRING', 'Bank name for payment', FALSE, FALSE),
('payment.bank.account-number', '1234567890', 'BANK', 'STRING', 'Bank account number', FALSE, FALSE),
('payment.bank.account-name', 'CHATBOT SaaS', 'BANK', 'STRING', 'Bank account name', FALSE, FALSE),
('payment.bank-api.provider', 'mock', 'BANK', 'STRING', 'Bank API provider (mock, vietqr, etc.)', FALSE, FALSE),
('payment.bank-api.api-url', 'http://localhost:3000/mock-bank', 'BANK', 'STRING', 'Bank API URL', FALSE, FALSE),
('payment.bank-api.api-key', 'dev-mock-key-12345', 'BANK', 'STRING', 'Bank API key', TRUE, TRUE),
('payment.bank-api.timeout', '30000', 'BANK', 'INTEGER', 'Bank API timeout in milliseconds', FALSE, FALSE),
('payment.bank-api.retry-attempts', '3', 'BANK', 'INTEGER', 'Bank API retry attempts', FALSE, FALSE),
('payment.bank-api.retry-delay', '1000', 'BANK', 'INTEGER', 'Bank API retry delay in milliseconds', FALSE, FALSE)
ON CONFLICT (config_key) DO NOTHING;
