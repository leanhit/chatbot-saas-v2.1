-- V36: Penny conversation context persistent storage
-- Stores serialized ConversationContext per (tenant, bot, user, platform)
CREATE TABLE IF NOT EXISTS penny_conversation_contexts (
    id UUID PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    bot_id VARCHAR(64) NOT NULL,
    user_id VARCHAR(255),
    platform VARCHAR(64),
    context_key VARCHAR(512) NOT NULL,
    status VARCHAR(32),
    context_data TEXT,
    message_count INTEGER,
    last_intent VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    last_updated TIMESTAMP NOT NULL,
    expires_at TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_penny_ctx_context_key ON penny_conversation_contexts(context_key);
CREATE INDEX IF NOT EXISTS idx_penny_ctx_tenant_bot ON penny_conversation_contexts(tenant_id, bot_id);
CREATE INDEX IF NOT EXISTS idx_penny_ctx_tenant_user_platform ON penny_conversation_contexts(tenant_id, user_id, platform);
CREATE INDEX IF NOT EXISTS idx_penny_ctx_expires_at ON penny_conversation_contexts(expires_at);
