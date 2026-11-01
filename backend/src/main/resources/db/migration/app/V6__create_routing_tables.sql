-- ============================================================
-- V6: Create Intelligent Routing Tables for App Database
-- ============================================================

-- Routing Rules Table
CREATE TABLE IF NOT EXISTS routing_rules (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    rule_type VARCHAR(50) NOT NULL,
    priority INTEGER NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_agent_id BIGINT,
    target_team_id BIGINT,
    target_skill_id BIGINT,
    conditions JSONB,
    max_concurrent_conversations INTEGER,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_routing_rule_type CHECK (rule_type IN ('SKILL_BASED', 'PRIORITY_BASED', 'ROUND_ROBIN', 'LEAST_BUSY', 'CUSTOM')),
    CONSTRAINT chk_routing_target_type CHECK (target_type IN ('AGENT', 'TEAM', 'SKILL', 'QUEUE'))
);

CREATE INDEX IF NOT EXISTS idx_routing_rules_tenant ON routing_rules(tenant_id);
CREATE INDEX IF NOT EXISTS idx_routing_rules_type ON routing_rules(rule_type);
CREATE INDEX IF NOT EXISTS idx_routing_rules_target_type ON routing_rules(target_type);
CREATE INDEX IF NOT EXISTS idx_routing_rules_priority ON routing_rules(priority);
CREATE INDEX IF NOT EXISTS idx_routing_rules_active ON routing_rules(is_active);
CREATE INDEX IF NOT EXISTS idx_routing_rules_default ON routing_rules(is_default);

COMMENT ON TABLE routing_rules IS 'Rules for intelligent conversation routing';
COMMENT ON COLUMN routing_rules.conditions IS 'JSONB conditions for rule matching';

-- Escalation Rules Table
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
CREATE INDEX IF NOT EXISTS idx_escalation_rules_trigger_type ON escalation_rules(trigger_type);
CREATE INDEX IF NOT EXISTS idx_escalation_rules_action ON escalation_rules(escalation_action);
CREATE INDEX IF NOT EXISTS idx_escalation_rules_priority ON escalation_rules(priority);
CREATE INDEX IF NOT EXISTS idx_escalation_rules_active ON escalation_rules(is_active);

COMMENT ON TABLE escalation_rules IS 'Rules for automatic conversation escalation';
COMMENT ON COLUMN escalation_rules.conditions IS 'JSONB conditions for trigger matching';

-- Supervisor Sessions Table
CREATE TABLE IF NOT EXISTS supervisor_sessions (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    supervisor_id BIGINT NOT NULL,
    conversation_id BIGINT NOT NULL,
    session_type VARCHAR(50) NOT NULL,
    session_status VARCHAR(50) NOT NULL,
    monitored_agent_id BIGINT,
    joined_at TIMESTAMP,
    left_at TIMESTAMP,
    duration_seconds INTEGER,
    is_silent BOOLEAN NOT NULL DEFAULT TRUE,
    can_intervene BOOLEAN NOT NULL DEFAULT FALSE,
    intervention_count INTEGER NOT NULL DEFAULT 0,
    notes TEXT,
    rating INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_supervisor_session_type CHECK (session_type IN ('MONITORING', 'COACHING', 'TAKEOVER', 'ASSISTING')),
    CONSTRAINT chk_supervisor_session_status CHECK (session_status IN ('ACTIVE', 'ENDED', 'INTERRUPTED')),
    CONSTRAINT chk_supervisor_rating CHECK (rating IS NULL OR (rating >= 1 AND rating <= 5))
);

CREATE INDEX IF NOT EXISTS idx_supervisor_sessions_tenant ON supervisor_sessions(tenant_id);
CREATE INDEX IF NOT EXISTS idx_supervisor_sessions_supervisor ON supervisor_sessions(supervisor_id);
CREATE INDEX IF NOT EXISTS idx_supervisor_sessions_conversation ON supervisor_sessions(conversation_id);
CREATE INDEX IF NOT EXISTS idx_supervisor_sessions_status ON supervisor_sessions(session_status);
CREATE INDEX IF NOT EXISTS idx_supervisor_sessions_type ON supervisor_sessions(session_type);
CREATE UNIQUE INDEX IF NOT EXISTS idx_supervisor_sessions_unique_active ON supervisor_sessions(supervisor_id, conversation_id) WHERE session_status = 'ACTIVE';

COMMENT ON TABLE supervisor_sessions IS 'Sessions for supervisor monitoring and takeover of conversations';
