-- Simple Payment System Migration
-- Add balance column to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS balance DECIMAL(15,2) DEFAULT 0.00;

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
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    completed_at TIMESTAMP,
    
    CONSTRAINT fk_simple_payments_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_simple_payments_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_simple_payments_user_tenant ON simple_payments(user_id, tenant_id);
CREATE INDEX IF NOT EXISTS idx_simple_payments_reference ON simple_payments(reference_code);
CREATE INDEX IF NOT EXISTS idx_simple_payments_status ON simple_payments(status);
CREATE INDEX IF NOT EXISTS idx_simple_payments_expires ON simple_payments(expires_at);

-- Update existing users to have balance = 0
UPDATE users SET balance = 0.00 WHERE balance IS NULL;

-- Insert bank configuration (optional)
INSERT INTO system_config (config_key, config_value, config_type, description) VALUES
('payment.bank.name', 'Vietcombank', 'STRING', 'Default bank for payments'),
('payment.bank.account_number', '1234567890', 'STRING', 'Bank account number'),
('payment.bank.account_name', 'CHATBOT SaaS', 'STRING', 'Bank account name'),
('payment.qr.expire_hours', '24', 'INTEGER', 'QR code expiration in hours'),
('payment.check.interval_seconds', '10', 'INTEGER', 'Payment check interval in seconds')
ON CONFLICT (config_key) DO NOTHING;
