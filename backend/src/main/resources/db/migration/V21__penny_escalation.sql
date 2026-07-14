-- ============================================================
-- V21: Create Escalation Tickets table for Penny Bot
-- ============================================================

CREATE TABLE IF NOT EXISTS penny_escalation_tickets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bot_id UUID,
    tenant_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    conversation_id VARCHAR(255),
    reason TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    assigned_agent_id VARCHAR(255),
    priority VARCHAR(20) DEFAULT 'NORMAL',
    confidence_score DOUBLE PRECISION,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT NOW(),
    resolved_at TIMESTAMP,
    resolution_notes TEXT,

    CONSTRAINT fk_escalation_bot FOREIGN KEY (bot_id) REFERENCES penny_bots(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_escalation_bot_tenant
    ON penny_escalation_tickets(bot_id, tenant_id);
CREATE INDEX IF NOT EXISTS idx_escalation_user
    ON penny_escalation_tickets(user_id);
CREATE INDEX IF NOT EXISTS idx_escalation_status
    ON penny_escalation_tickets(status, created_at);
CREATE INDEX IF NOT EXISTS idx_escalation_agent
    ON penny_escalation_tickets(assigned_agent_id) WHERE assigned_agent_id IS NOT NULL;
