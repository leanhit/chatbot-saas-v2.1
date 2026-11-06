-- Phase 5: Tenant-scoped daily analytics summaries (aggregated from conversations/messages/escalations)
CREATE TABLE IF NOT EXISTS analytics_daily_summary (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT,
    tenant_key VARCHAR(255),
    summary_date DATE NOT NULL,
    total_conversations INTEGER NOT NULL DEFAULT 0,
    new_conversations INTEGER NOT NULL DEFAULT 0,
    closed_conversations INTEGER NOT NULL DEFAULT 0,
    total_messages INTEGER NOT NULL DEFAULT 0,
    user_messages INTEGER NOT NULL DEFAULT 0,
    bot_messages INTEGER NOT NULL DEFAULT 0,
    agent_messages INTEGER NOT NULL DEFAULT 0,
    escalations_count INTEGER NOT NULL DEFAULT 0,
    avg_first_response_minutes DOUBLE PRECISION,
    avg_resolution_minutes DOUBLE PRECISION,
    avg_sentiment_score DOUBLE PRECISION,
    negative_sentiment_count INTEGER NOT NULL DEFAULT 0,
    ai_suggestions_count INTEGER NOT NULL DEFAULT 0,
    ai_suggestions_accepted INTEGER NOT NULL DEFAULT 0,
    ai_suggestions_rejected INTEGER NOT NULL DEFAULT 0,
    metrics JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_analytics_daily_tenant_date UNIQUE (tenant_id, summary_date)
);

CREATE INDEX IF NOT EXISTS idx_analytics_daily_tenant_date ON analytics_daily_summary(tenant_id, summary_date);
