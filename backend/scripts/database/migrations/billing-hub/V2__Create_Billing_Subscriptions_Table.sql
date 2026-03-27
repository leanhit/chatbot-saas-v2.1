-- Create updated_at function if not exists
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create Billing Subscriptions Table
CREATE TABLE IF NOT EXISTS billing_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    billing_account_id BIGINT NOT NULL REFERENCES billing_accounts(id) ON DELETE CASCADE,
    subscription_name VARCHAR(255) NOT NULL,
    plan_name VARCHAR(100) NOT NULL,
    plan_type VARCHAR(50) NOT NULL, -- MONTHLY, YEARLY, USAGE_BASED
    amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'USD',
    billing_cycle VARCHAR(50) NOT NULL, -- MONTHLY, YEARLY
    next_billing_date DATE,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    features JSONB,
    usage_limits JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_billing_subscriptions_account_id ON billing_subscriptions(billing_account_id);
CREATE INDEX IF NOT EXISTS idx_billing_subscriptions_status ON billing_subscriptions(status);
CREATE INDEX IF NOT EXISTS idx_billing_subscriptions_next_billing ON billing_subscriptions(next_billing_date);

-- Create trigger for updated_at
CREATE TRIGGER update_billing_subscriptions_updated_at 
    BEFORE UPDATE ON billing_subscriptions 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
