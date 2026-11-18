-- Phase 5: Conversation sentiment scoring (display/alert only, no auto-escalation)
CREATE TABLE IF NOT EXISTS conversation_sentiment (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT,
    tenant_key VARCHAR(255),
    conversation_id BIGINT NOT NULL,
    message_id BIGINT,
    score DOUBLE PRECISION NOT NULL,
    label VARCHAR(20) NOT NULL,
    source VARCHAR(20) NOT NULL DEFAULT 'LEXICON',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_sentiment_score CHECK (score >= -1 AND score <= 1),
    CONSTRAINT chk_sentiment_label CHECK (label IN ('POSITIVE', 'NEUTRAL', 'NEGATIVE')),
    CONSTRAINT chk_sentiment_source CHECK (source IN ('AI', 'LEXICON'))
);

CREATE INDEX IF NOT EXISTS idx_sentiment_tenant ON conversation_sentiment(tenant_id);
CREATE INDEX IF NOT EXISTS idx_sentiment_conversation ON conversation_sentiment(conversation_id);
CREATE INDEX IF NOT EXISTS idx_sentiment_conversation_created ON conversation_sentiment(conversation_id, created_at);
CREATE INDEX IF NOT EXISTS idx_sentiment_label ON conversation_sentiment(tenant_id, label);
