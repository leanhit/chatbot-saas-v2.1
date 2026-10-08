-- ============================================================
-- V2: Create addresses table
-- ============================================================

CREATE TABLE IF NOT EXISTS addresses (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT,
    owner_type VARCHAR(30) NOT NULL,
    owner_id BIGINT NOT NULL,
    house_number VARCHAR(255),
    street VARCHAR(255),
    ward VARCHAR(255),
    district VARCHAR(255),
    province VARCHAR(255),
    country VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_address_tenant ON addresses(tenant_id);
CREATE INDEX IF NOT EXISTS idx_address_owner ON addresses(tenant_id, owner_type, owner_id);
