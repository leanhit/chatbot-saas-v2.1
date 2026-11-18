-- ============================================================
-- V28: Create Escalation History table for tracking escalation events
-- ============================================================

CREATE TABLE IF NOT EXISTS escalation_history (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    conversation_id BIGINT NOT NULL,
    escalated_to_agent_id BIGINT,
    escalated_to_team_id BIGINT,
    escalation_rule_id BIGINT,
    escalation_trigger_type VARCHAR(50) NOT NULL,
    escalation_action VARCHAR(50) NOT NULL,
    escalated_to_supervisor BOOLEAN,
    supervisor_notified BOOLEAN,
    escalated BOOLEAN NOT NULL,
    reason TEXT,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_escalation_trigger_type CHECK (escalation_trigger_type IN ('WAIT_TIME_EXCEEDED', 'NO_RESPONSE', 'LOW_CUSTOMER_SATISFACTION', 'HIGH_PRIORITY_CUSTOMER', 'CUSTOMER_TIER', 'CUSTOM_CONDITION')),
    CONSTRAINT chk_escalation_action CHECK (escalation_action IN ('ASSIGN_TO_AGENT', 'ASSIGN_TO_TEAM', 'ASSIGN_TO_SUPERVISOR', 'MOVE_TO_QUEUE', 'SEND_NOTIFICATION'))
);

CREATE INDEX IF NOT EXISTS idx_escalation_history_tenant ON escalation_history(tenant_id);
CREATE INDEX IF NOT EXISTS idx_escalation_history_conversation ON escalation_history(conversation_id);
CREATE INDEX IF NOT EXISTS idx_escalation_history_agent ON escalation_history(escalated_to_agent_id);
CREATE INDEX IF NOT EXISTS idx_escalation_history_team ON escalation_history(escalated_to_team_id);
CREATE INDEX IF NOT EXISTS idx_escalation_history_rule ON escalation_history(escalation_rule_id);
CREATE INDEX IF NOT EXISTS idx_escalation_history_created_at ON escalation_history(created_at DESC);

COMMENT ON TABLE escalation_history IS 'History of escalation events for audit and analysis';
