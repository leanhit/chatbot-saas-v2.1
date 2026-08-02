-- Simple Payment System Migration
-- Note: Cross-database foreign keys removed - validation handled at application level

-- Add balance column to users table if it exists
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'users') THEN
        ALTER TABLE users ADD COLUMN IF NOT EXISTS balance DECIMAL(15,2) DEFAULT 0.00;
        UPDATE users SET balance = 0.00 WHERE balance IS NULL;
    END IF;
END $$;

-- Create simple_payments table
CREATE TABLE IF NOT EXISTS simple_payments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'VND',
    reference_code VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    bank_transaction_id VARCHAR(100),
    description TEXT,
    qr_content TEXT,
    expires_at TIMESTAMP NOT NULL,
    target_package_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    completed_at TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_simple_payments_user_tenant ON simple_payments(user_id, tenant_id);
CREATE INDEX IF NOT EXISTS idx_simple_payments_reference ON simple_payments(reference_code);
CREATE INDEX IF NOT EXISTS idx_simple_payments_status ON simple_payments(status);
CREATE INDEX IF NOT EXISTS idx_simple_payments_expires ON simple_payments(expires_at);

-- Insert bank configuration (optional)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'system_configs') THEN
        INSERT INTO system_configs (config_key, config_value, config_category, config_type, description, is_sensitive, is_encrypted, created_at) VALUES
        ('payment.bank.name', 'Vietcombank', 'BANK', 'STRING', 'Default bank for payments', FALSE, FALSE, CURRENT_TIMESTAMP),
        ('payment.bank.account_number', '1234567890', 'BANK', 'STRING', 'Bank account number', FALSE, FALSE, CURRENT_TIMESTAMP),
        ('payment.bank.account_name', 'CHATBOT SaaS', 'BANK', 'STRING', 'Bank account name', FALSE, FALSE, CURRENT_TIMESTAMP),
        ('payment.qr.expire_hours', '24', 'BANK', 'INTEGER', 'QR code expiration in hours', FALSE, FALSE, CURRENT_TIMESTAMP),
        ('payment.check.interval_seconds', '10', 'BANK', 'INTEGER', 'Payment check interval in seconds', FALSE, FALSE, CURRENT_TIMESTAMP)
        ON CONFLICT (config_key) DO NOTHING;
    END IF;
END $$;
