-- Per-bot public API keys for Penny public chat endpoint
CREATE TABLE IF NOT EXISTS penny_bot_api_keys (
    id UUID PRIMARY KEY,
    bot_id UUID NOT NULL REFERENCES penny_bots(id) ON DELETE CASCADE,
    tenant_id BIGINT NOT NULL,
    key_hash VARCHAR(64) NOT NULL,          -- SHA-256 hex of the plaintext key
    key_prefix VARCHAR(16) NOT NULL,        -- first chars for identification (never the full key)
    name VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    expires_at TIMESTAMP,
    last_used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_penny_bot_api_keys_bot ON penny_bot_api_keys(bot_id);
CREATE INDEX IF NOT EXISTS idx_penny_bot_api_keys_hash ON penny_bot_api_keys(key_hash);
CREATE INDEX IF NOT EXISTS idx_penny_bot_api_keys_tenant ON penny_bot_api_keys(tenant_id);
