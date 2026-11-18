-- ============================================================
-- V2: Create facebook_users table for Facebook user management
-- ============================================================

CREATE TABLE IF NOT EXISTS facebook_users (
    id BIGSERIAL PRIMARY KEY,
    tenant_key VARCHAR(255),
    tenant_id BIGINT NOT NULL,
    psid VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    profile_pic VARCHAR(1000),
    odoo_partner_id INTEGER,
    page_id VARCHAR(255) NOT NULL,
    last_interaction TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT uk_facebook_user_tenant_psid UNIQUE (tenant_id, psid)
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_facebook_user_psid ON facebook_users(psid);
CREATE INDEX IF NOT EXISTS idx_facebook_user_odoo_partner ON facebook_users(odoo_partner_id);
CREATE INDEX IF NOT EXISTS idx_facebook_user_page ON facebook_users(page_id);
CREATE INDEX IF NOT EXISTS idx_facebook_user_tenant ON facebook_users(tenant_id);

-- Add comments
COMMENT ON TABLE facebook_users IS 'Facebook user information';
COMMENT ON COLUMN facebook_users.psid IS 'Facebook Page-scoped ID';
COMMENT ON COLUMN facebook_users.odoo_partner_id IS 'Odoo partner ID for integration';
COMMENT ON COLUMN facebook_users.page_id IS 'Facebook page ID';
COMMENT ON COLUMN facebook_users.last_interaction IS 'Last interaction timestamp';
