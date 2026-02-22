-- Create Wallets Table
CREATE TABLE IF NOT EXISTS wallets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    tenant_id UUID,
    currency VARCHAR(10) NOT NULL DEFAULT 'USD',
    balance DECIMAL(19,4) DEFAULT 0.00 CHECK (balance >= 0),
    available_balance DECIMAL(19,4) DEFAULT 0.00 CHECK (available_balance >= 0),
    frozen_balance DECIMAL(19,4) DEFAULT 0.00 CHECK (frozen_balance >= 0),
    status VARCHAR(50) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'FROZEN', 'CLOSED', 'SUSPENDED')),
    wallet_type VARCHAR(50) DEFAULT 'PERSONAL' CHECK (wallet_type IN ('PERSONAL', 'BUSINESS', 'TEAM')),
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Ensure balance consistency
    CONSTRAINT chk_balance_consistency CHECK (balance = available_balance + frozen_balance)
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_wallets_user_id ON wallets(user_id);
CREATE INDEX IF NOT EXISTS idx_wallets_tenant_id ON wallets(tenant_id);
CREATE INDEX IF NOT EXISTS idx_wallets_currency ON wallets(currency);
CREATE INDEX IF NOT EXISTS idx_wallets_status ON wallets(status);
CREATE INDEX IF NOT EXISTS idx_wallets_type ON wallets(wallet_type);
CREATE INDEX IF NOT EXISTS idx_wallets_created_at ON wallets(created_at);

-- Create unique constraint for user wallet per currency
CREATE UNIQUE INDEX IF NOT EXISTS idx_wallets_user_currency ON wallets(user_id, currency) 
WHERE status != 'CLOSED';

-- Create GIN index for metadata JSONB
CREATE INDEX IF NOT EXISTS idx_wallets_metadata ON wallets USING GIN(metadata);

-- Create trigger for updated_at
CREATE OR REPLACE FUNCTION update_wallets_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_wallets_updated_at 
    BEFORE UPDATE ON wallets 
    FOR EACH ROW 
    EXECUTE FUNCTION update_wallets_updated_at_column();

-- Function to check wallet balance sufficiency
CREATE OR REPLACE FUNCTION check_wallet_balance(
    p_wallet_id UUID,
    p_amount DECIMAL(19,4)
) RETURNS BOOLEAN AS $$
DECLARE
    v_available_balance DECIMAL(19,4);
BEGIN
    SELECT available_balance INTO v_available_balance
    FROM wallets
    WHERE id = p_wallet_id AND status = 'ACTIVE';
    
    RETURN v_available_balance >= p_amount;
END;
$$ LANGUAGE plpgsql;
