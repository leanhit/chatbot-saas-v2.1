-- Create Transactions Table
CREATE TABLE IF NOT EXISTS transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id UUID NOT NULL,
    transaction_type VARCHAR(50) NOT NULL CHECK (transaction_type IN ('TOPUP', 'PURCHASE', 'REFUND', 'FEE', 'TRANSFER_IN', 'TRANSFER_OUT')),
    amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED')),
    description TEXT,
    fee DECIMAL(19,4) DEFAULT 0.00 CHECK (fee >= 0),
    net_amount DECIMAL(19,4),
    metadata JSONB,
    external_transaction_id TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    
    -- Ensure amount is positive for most transaction types
    CONSTRAINT chk_amount_positive CHECK (amount > 0),
    
    -- Ensure net_amount is calculated correctly
    CONSTRAINT chk_net_amount CHECK (
        (transaction_type = 'TOPUP' AND net_amount = amount - fee) OR
        (transaction_type = 'PURCHASE' AND net_amount = amount + fee) OR
        (transaction_type = 'REFUND' AND net_amount = amount - fee) OR
        (transaction_type = 'FEE' AND net_amount = -amount) OR
        (transaction_type IN ('TRANSFER_IN', 'TRANSFER_OUT'))
    )
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_transactions_wallet_id ON transactions(wallet_id);
CREATE INDEX IF NOT EXISTS idx_transactions_type ON transactions(transaction_type);
CREATE INDEX IF NOT EXISTS idx_transactions_status ON transactions(status);
CREATE INDEX IF NOT EXISTS idx_transactions_currency ON transactions(currency);
CREATE INDEX IF NOT EXISTS idx_transactions_created_at ON transactions(created_at);
CREATE INDEX IF NOT EXISTS idx_transactions_completed_at ON transactions(completed_at);

-- Create composite index for wallet transactions
CREATE INDEX IF NOT EXISTS idx_transactions_wallet_created ON transactions(wallet_id, created_at DESC);

-- Create unique index for external transaction IDs
CREATE UNIQUE INDEX IF NOT EXISTS idx_transactions_external_id ON transactions(external_transaction_id) 
WHERE external_transaction_id IS NOT NULL;

-- Create GIN index for metadata JSONB
CREATE INDEX IF NOT EXISTS idx_transactions_metadata ON transactions USING GIN(metadata);

-- Add foreign key constraint to wallets
ALTER TABLE transactions 
ADD CONSTRAINT fk_transactions_wallet 
FOREIGN KEY (wallet_id) REFERENCES wallets(id);

-- Create trigger for completed_at
CREATE OR REPLACE FUNCTION set_transaction_completed_at()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 'COMPLETED' AND OLD.status != 'COMPLETED' THEN
        NEW.completed_at = CURRENT_TIMESTAMP;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_transaction_completed_at_trigger
    BEFORE UPDATE ON transactions
    FOR EACH ROW
    EXECUTE FUNCTION set_transaction_completed_at();
