-- Create updated_at function if not exists
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create App Subscriptions Table
CREATE TABLE IF NOT EXISTS app_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    app_id BIGINT NOT NULL REFERENCES app_registry(id) ON DELETE CASCADE,
    tenant_id BIGINT NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    plan VARCHAR(50),
    configuration JSONB,
    usage_stats JSONB,
    billing_info JSONB,
    subscribed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(app_id, tenant_id)
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_subscriptions_app_id ON app_subscriptions(app_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_tenant_id ON app_subscriptions(tenant_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_status ON app_subscriptions(status);

-- Create trigger for updated_at
CREATE TRIGGER update_app_subscriptions_updated_at 
    BEFORE UPDATE ON app_subscriptions 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
