-- ============================================================
-- V17: Create package_upgrade_audit and invoices tables
-- ============================================================

-- Create package_upgrade_audit table
CREATE TABLE IF NOT EXISTS package_upgrade_audit (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    payment_reference_code VARCHAR(255) NOT NULL,
    from_package_id VARCHAR(255),
    to_package_id VARCHAR(255) NOT NULL,
    payment_amount NUMERIC(15, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'VND',
    bank_transaction_id VARCHAR(255),
    upgrade_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    failure_reason TEXT,
    processed_at TIMESTAMP,
    ip_address VARCHAR(100),
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_package_upgrade_audit_tenant_id ON package_upgrade_audit(tenant_id);
CREATE INDEX IF NOT EXISTS idx_package_upgrade_audit_user_id ON package_upgrade_audit(user_id);
CREATE INDEX IF NOT EXISTS idx_package_upgrade_audit_ref_code ON package_upgrade_audit(payment_reference_code);

-- Create invoices table
CREATE TABLE IF NOT EXISTS invoices (
    id BIGSERIAL PRIMARY KEY,
    invoice_number VARCHAR(50) UNIQUE NOT NULL,
    payment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    user_email VARCHAR(100) NOT NULL,
    user_name VARCHAR(100) NOT NULL,
    subtotal NUMERIC(15, 2) NOT NULL,
    discount_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    tax_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(15, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'VND',
    discount_code VARCHAR(50),
    package_id VARCHAR(50),
    package_name VARCHAR(100),
    status VARCHAR(50) NOT NULL DEFAULT 'PAID',
    notes TEXT,
    payment_method VARCHAR(50),
    payment_reference TEXT,
    due_date TIMESTAMP,
    paid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_invoices_tenant_id ON invoices(tenant_id);
CREATE INDEX IF NOT EXISTS idx_invoices_user_id ON invoices(user_id);
CREATE INDEX IF NOT EXISTS idx_invoices_payment_id ON invoices(payment_id);
CREATE INDEX IF NOT EXISTS idx_invoices_invoice_number ON invoices(invoice_number);

COMMENT ON TABLE package_upgrade_audit IS 'Audit log of subscription package upgrades and downgrades';
COMMENT ON TABLE invoices IS 'Stores billing invoices for payment transactions';
