-- Add target_package_id column to simple_payments table
ALTER TABLE simple_payments 
ADD COLUMN target_package_id VARCHAR(50);

-- Create package_upgrade_audit table
CREATE TABLE package_upgrade_audit (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    payment_reference_code VARCHAR(50) NOT NULL,
    from_package_id VARCHAR(50),
    to_package_id VARCHAR(50) NOT NULL,
    payment_amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'VND',
    bank_transaction_id VARCHAR(100),
    upgrade_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    failure_reason TEXT,
    processed_at TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_upgrade_audit_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT fk_upgrade_audit_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Add indexes for performance
CREATE INDEX idx_upgrade_audit_tenant ON package_upgrade_audit(tenant_id);
CREATE INDEX idx_upgrade_audit_user ON package_upgrade_audit(user_id);
CREATE INDEX idx_upgrade_audit_payment_ref ON package_upgrade_audit(payment_reference_code);
CREATE INDEX idx_upgrade_audit_status ON package_upgrade_audit(upgrade_status);
CREATE INDEX idx_upgrade_audit_created_at ON package_upgrade_audit(created_at);

-- Add comments
COMMENT ON TABLE package_upgrade_audit IS 'Audit trail for automatic package upgrades after payment completion';
COMMENT ON COLUMN package_upgrade_audit.upgrade_status IS 'SUCCESS, FAILED, PENDING';
COMMENT ON COLUMN simple_payments.target_package_id IS 'Target package ID for automatic upgrade after payment';
