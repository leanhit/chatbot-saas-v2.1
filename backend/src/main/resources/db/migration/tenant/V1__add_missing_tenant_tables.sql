-- V4__add_missing_tenant_tables.sql
-- Create missing tables for tenant module to match entities
-- Note: Cross-database foreign keys removed - validation handled at application level

CREATE TABLE IF NOT EXISTS tenant_profiles (
    tenant_id BIGINT PRIMARY KEY,
    description VARCHAR(1000),
    industry VARCHAR(100),
    plan VARCHAR(50),
    company_size VARCHAR(50),
    legal_name VARCHAR(255),
    tax_code VARCHAR(255),
    contact_email VARCHAR(255),
    contact_phone VARCHAR(255),
    website VARCHAR(255),
    logo_url VARCHAR(255),
    favicon_url VARCHAR(255),
    primary_color VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tenant_profile_tenant ON tenant_profiles(tenant_id);

CREATE TABLE IF NOT EXISTS tenant_professionals (
    tenant_id BIGINT PRIMARY KEY,
    job_title VARCHAR(255),
    department VARCHAR(255),
    company VARCHAR(200),
    linkedin_url VARCHAR(255),
    website VARCHAR(255),
    location VARCHAR(255),
    skills TEXT,
    experience TEXT,
    education TEXT,
    certifications TEXT,
    languages VARCHAR(255),
    availability VARCHAR(255),
    hourly_rate VARCHAR(100),
    portfolio_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tenant_professional_tenant ON tenant_professionals(tenant_id);

CREATE TABLE IF NOT EXISTS tenant_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    action VARCHAR(255) NOT NULL,
    details TEXT,
    ip_address VARCHAR(100),
    user_agent VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tenant_audit_tenant ON tenant_audit_logs(tenant_id);
CREATE INDEX IF NOT EXISTS idx_tenant_audit_user ON tenant_audit_logs(user_id);
