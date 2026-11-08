-- V34: Dead letter queue for failed outbound bot-rule webhook calls
CREATE TABLE IF NOT EXISTS bot_webhook_dead_letters (
    id BIGSERIAL PRIMARY KEY,
    rule_id UUID NOT NULL,
    bot_id UUID NOT NULL,
    rule_name VARCHAR(200),
    webhook_url VARCHAR(500) NOT NULL,
    payload TEXT,
    attempts INTEGER NOT NULL,
    last_error TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_bot_webhook_dlq_status ON bot_webhook_dead_letters(status);
CREATE INDEX IF NOT EXISTS idx_bot_webhook_dlq_bot ON bot_webhook_dead_letters(bot_id);
