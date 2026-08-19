-- ============================================================
-- V0: Create tenants table (Base table)
-- ============================================================

CREATE TABLE IF NOT EXISTS tenants (
    id BIGSERIAL PRIMARY KEY,
    tenantkey VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    visibility VARCHAR(50) NOT NULL DEFAULT 'PUBLIC',
    expires_at TIMESTAMP,
    current_package_id VARCHAR(100),
    package_activated_at TIMESTAMP,
    createdat TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedat TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tenants_tenantkey ON tenants(tenantkey);
CREATE INDEX IF NOT EXISTS idx_tenants_status ON tenants(status);
CREATE INDEX IF NOT EXISTS idx_tenants_visibility ON tenants(visibility);
