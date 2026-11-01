-- ============================================================
-- V26: Create Supervisor Sessions table for Supervisor Monitoring
-- ============================================================

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
    CONSTRAINT chk_supervisor_rating CHECK (rating IS NULL OR (rating >= 1 AND rating <= 5)),
    CONSTRAINT uk_supervisor_conversation UNIQUE (supervisor_id, conversation_id, session_status) WHERE session_status = 'ACTIVE'
);

CREATE INDEX IF NOT EXISTS idx_supervisor_sessions_tenant ON supervisor_sessions(tenant_id);
CREATE INDEX IF NOT EXISTS idx_supervisor_sessions_supervisor ON supervisor_sessions(supervisor_id);
CREATE INDEX IF NOT EXISTS idx_supervisor_sessions_conversation ON supervisor_sessions(conversation_id);
CREATE INDEX IF NOT EXISTS idx_supervisor_sessions_status ON supervisor_sessions(session_status);
CREATE INDEX IF NOT EXISTS idx_supervisor_sessions_active ON supervisor_sessions(supervisor_id, conversation_id, session_status) WHERE session_status = 'ACTIVE';

COMMENT ON TABLE supervisor_sessions IS 'Tracks supervisor monitoring, coaching, and takeover sessions for agent conversations';
