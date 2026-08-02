-- ============================================================
-- V0: Create facebook_connection table (Base table)
-- ============================================================

CREATE TABLE IF NOT EXISTS facebook_connection (
    id UUID PRIMARY KEY,
    bot_id VARCHAR(255),
    bot_name VARCHAR(255),
    owner_id VARCHAR(255),
    page_id VARCHAR(255),
    fanpage_url TEXT,
    page_access_token TEXT,
    user_access_token TEXT,
    token_updated_at TIMESTAMP,
    token_expires_at TIMESTAMP,
    fb_user_id VARCHAR(255),
    is_enabled BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    chatbot_provider VARCHAR(20) DEFAULT 'PENNYBOT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tenant_id BIGINT NOT NULL,
    CONSTRAINT uk_facebook_connection_tenant_page UNIQUE (tenant_id, page_id)
);

CREATE INDEX IF NOT EXISTS idx_facebook_connection_tenant ON facebook_connection(tenant_id);
CREATE INDEX IF NOT EXISTS idx_facebook_connection_page ON facebook_connection(page_id);
CREATE INDEX IF NOT EXISTS idx_facebook_connection_owner ON facebook_connection(owner_id);
