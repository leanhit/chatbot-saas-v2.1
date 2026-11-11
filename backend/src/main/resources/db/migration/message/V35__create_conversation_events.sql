-- V35: Conversation lifecycle events (audit/timeline)
CREATE TABLE IF NOT EXISTS conversation_events (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    tenant_key VARCHAR(255),
    conversation_id BIGINT NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    actor_type VARCHAR(16),
    actor_id VARCHAR(128),
    from_value VARCHAR(255),
    to_value VARCHAR(255),
    metadata JSONB,
    occurred_at TIMESTAMP NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_conv_event_tenant ON conversation_events(tenant_id);
CREATE INDEX IF NOT EXISTS idx_conv_event_conversation ON conversation_events(conversation_id);
CREATE INDEX IF NOT EXISTS idx_conv_event_type ON conversation_events(event_type);
CREATE INDEX IF NOT EXISTS idx_conv_event_occurred ON conversation_events(occurred_at);
