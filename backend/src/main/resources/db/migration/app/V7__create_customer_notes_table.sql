-- Phase 4: Customer notes (agent notes on customer profiles)
CREATE TABLE IF NOT EXISTS customer_notes (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT,
    tenant_key VARCHAR(255),
    customer_profile_id BIGINT NOT NULL,
    author_id VARCHAR(255),
    author_name VARCHAR(255),
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_customer_notes_tenant ON customer_notes(tenant_id);
CREATE INDEX IF NOT EXISTS idx_customer_notes_profile ON customer_notes(customer_profile_id);
CREATE INDEX IF NOT EXISTS idx_customer_notes_profile_created ON customer_notes(customer_profile_id, created_at);
