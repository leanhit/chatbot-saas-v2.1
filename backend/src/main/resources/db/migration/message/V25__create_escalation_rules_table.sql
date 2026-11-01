-- ============================================================
-- V25: Create Escalation Rules table for Intelligent Escalation
-- ============================================================

CREATE TABLE IF NOT EXISTS escalation_rules (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    trigger_type VARCHAR(50) NOT NULL,
    trigger_value VARCHAR(255),
    threshold_value INTEGER,
    time_threshold_minutes INTEGER,
    conditions JSONB,
    escalation_action VARCHAR(50) NOT NULL,
    escalate_to_agent_id BIGINT,
    escalate_to_team_id BIGINT,
    escalate_to_supervisor BOOLEAN NOT NULL DEFAULT FALSE,
    notify_supervisor BOOLEAN NOT NULL DEFAULT TRUE,
    priority INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_escalation_trigger_type CHECK (trigger_type IN ('WAIT_TIME_EXCEEDED', 'NO_RESPONSE', 'LOW_CUSTOMER_SATISFACTION', 'HIGH_PRIORITY_CUSTOMER', 'CUSTOMER_TIER', 'CUSTOM_CONDITION')),
    CONSTRAINT chk_escalation_action CHECK (escalation_action IN ('ASSIGN_TO_AGENT', 'ASSIGN_TO_TEAM', 'ASSIGN_TO_SUPERVISOR', 'MOVE_TO_QUEUE', 'SEND_NOTIFICATION'))
);

CREATE INDEX IF NOT EXISTS idx_escalation_rules_tenant ON escalation_rules(tenant_id);
CREATE INDEX IF NOT EXISTS idx_escalation_rules_priority ON escalation_rules(priority);
CREATE INDEX IF NOT EXISTS idx_escalation_rules_active ON escalation_rules(tenant_id, is_active);
CREATE INDEX IF NOT EXISTS idx_escalation_rules_trigger ON escalation_rules(trigger_type);

COMMENT ON TABLE escalation_rules IS 'Rules for automatic escalation based on triggers like wait time, no response, or customer tier';
