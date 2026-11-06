-- Phase 5: AI suggestions for agent assistance (display-only, no auto-actions)
CREATE TABLE IF NOT EXISTS ai_suggestions (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT,
    tenant_key VARCHAR(255),
    conversation_id BIGINT NOT NULL,
    agent_id BIGINT,
    suggestion_text TEXT NOT NULL,
    confidence DOUBLE PRECISION,
    provider VARCHAR(50),
    model VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    response_note TEXT,
    responded_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_ai_suggestion_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'EXPIRED')),
    CONSTRAINT chk_ai_suggestion_confidence CHECK (confidence IS NULL OR (confidence >= 0 AND confidence <= 1))
);

CREATE INDEX IF NOT EXISTS idx_ai_suggestion_tenant ON ai_suggestions(tenant_id);
CREATE INDEX IF NOT EXISTS idx_ai_suggestion_conversation ON ai_suggestions(conversation_id);
CREATE INDEX IF NOT EXISTS idx_ai_suggestion_tenant_status ON ai_suggestions(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_ai_suggestion_created ON ai_suggestions(created_at);
