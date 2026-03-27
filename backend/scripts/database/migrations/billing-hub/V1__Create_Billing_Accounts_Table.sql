-- Create updated_at function if not exists
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create Billing Accounts Table
CREATE TABLE IF NOT EXISTS billing_accounts (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    account_name VARCHAR(255) NOT NULL,
    account_type VARCHAR(50) DEFAULT 'BUSINESS',
    currency VARCHAR(10) NOT NULL DEFAULT 'USD',
    billing_email VARCHAR(255),
    billing_address JSONB,
    payment_methods JSONB,
    credit_limit DECIMAL(19,4) DEFAULT 0.00,
    current_balance DECIMAL(19,4) DEFAULT 0.00,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_billing_accounts_tenant_id ON billing_accounts(tenant_id);
CREATE INDEX IF NOT EXISTS idx_billing_accounts_status ON billing_accounts(status);
CREATE INDEX IF NOT EXISTS idx_billing_accounts_currency ON billing_accounts(currency);

-- Create trigger for updated_at
CREATE TRIGGER update_billing_accounts_updated_at 
    BEFORE UPDATE ON billing_accounts 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
