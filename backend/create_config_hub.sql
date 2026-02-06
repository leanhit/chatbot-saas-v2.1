-- Create config_entries table for Config Hub
CREATE TABLE config_entries (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL,
    config_value VARCHAR(1000) NOT NULL,
    scope VARCHAR(20) NOT NULL CHECK (scope IN ('SYSTEM', 'TENANT', 'APP', 'TENANT_APP')),
    scope_id UUID,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX idx_config_entries_key_scope ON config_entries(config_key, scope);
CREATE INDEX idx_config_entries_scope_id ON config_entries(scope, scope_id);
CREATE UNIQUE INDEX idx_config_entries_unique ON config_entries(config_key, scope, COALESCE(scope_id, '00000000-0000-0000-0000-000000000000'::uuid));

-- Insert default system configurations
INSERT INTO config_entries (config_key, config_value, scope, description) VALUES
('system.default_locale', 'vi', 'SYSTEM', 'Default locale for the system'),
('system.default_plan', 'FREE', 'SYSTEM', 'Default billing plan for new tenants'),
('system.wallet.initial_balance', '0.00', 'SYSTEM', 'Initial wallet balance for new tenants'),
('system.auth.default_source', 'IDENTITY', 'SYSTEM', 'Default authentication source'),
('system.auth.default_provider', 'LOCAL', 'SYSTEM', 'Default authentication provider'),
('system.jwt.default_issuer', 'identity-hub', 'SYSTEM', 'Default JWT issuer'),
('system.app.chatbot.enabled', 'true', 'SYSTEM', 'Chatbot app enabled by default'),
('system.app.erp.enabled', 'false', 'SYSTEM', 'ERP app disabled by default'),
('system.app.crm.enabled', 'false', 'SYSTEM', 'CRM app disabled by default');

-- Create trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_config_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER trigger_config_entries_updated_at
    BEFORE UPDATE ON config_entries
    FOR EACH ROW
    EXECUTE FUNCTION update_config_updated_at();
