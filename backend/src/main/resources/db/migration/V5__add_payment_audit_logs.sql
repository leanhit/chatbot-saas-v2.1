-- Payment Audit Logs Migration
-- Create payment_audit_logs table for tracking all payment operations

CREATE TABLE IF NOT EXISTS payment_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    payment_reference_code VARCHAR(50) NOT NULL,
    user_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    old_status VARCHAR(20),
    new_status VARCHAR(20),
    amount DECIMAL(15,2),
    description TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    request_id VARCHAR(100),
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_payment_audit_logs_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_payment_audit_logs_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_payment_audit_logs_payment_reference ON payment_audit_logs(payment_reference_code);
CREATE INDEX IF NOT EXISTS idx_payment_audit_logs_user_id ON payment_audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_payment_audit_logs_tenant_id ON payment_audit_logs(tenant_id);
CREATE INDEX IF NOT EXISTS idx_payment_audit_logs_action ON payment_audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_payment_audit_logs_created_at ON payment_audit_logs(created_at);

-- Add target_package_id column to simple_payments table if not exists
ALTER TABLE simple_payments 
ADD COLUMN IF NOT EXISTS target_package_id VARCHAR(100);

-- Add comment to tables
COMMENT ON TABLE payment_audit_logs IS 'Audit log table for tracking all payment operations';
COMMENT ON COLUMN payment_audit_logs.action IS 'Action type: PAYMENT_CREATED, PAYMENT_COMPLETED, PAYMENT_FAILED, etc.';
COMMENT ON COLUMN payment_audit_logs.metadata IS 'Additional metadata in JSONB format';
COMMENT ON COLUMN simple_payments.target_package_id IS 'ID of the target package if payment is for package purchase';
