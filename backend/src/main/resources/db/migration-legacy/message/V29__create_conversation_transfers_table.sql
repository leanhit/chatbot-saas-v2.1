-- ============================================================
-- V29: Create Conversation Transfers table for tracking agent transfers
-- ============================================================

CREATE TABLE IF NOT EXISTS conversation_transfers (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    conversation_id BIGINT NOT NULL,
    from_agent_id BIGINT NOT NULL,
    to_agent_id BIGINT NOT NULL,
    transfer_reason TEXT,
    transfer_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    notes TEXT,
    transferred_by BIGINT,
    accepted_at TIMESTAMP,
    rejected_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_transfer_type CHECK (transfer_type IN ('MANUAL', 'AUTO_ESCALATION', 'WORKLOAD_BALANCE', 'SKILL_MISMATCH', 'SUPERVISOR_REQUEST')),
    CONSTRAINT chk_transfer_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'COMPLETED', 'CANCELLED'))
);

CREATE INDEX IF NOT EXISTS idx_transfer_tenant ON conversation_transfers(tenant_id);
CREATE INDEX IF NOT EXISTS idx_transfer_conversation ON conversation_transfers(conversation_id);
CREATE INDEX IF NOT EXISTS idx_transfer_from_agent ON conversation_transfers(from_agent_id);
CREATE INDEX IF NOT EXISTS idx_transfer_to_agent ON conversation_transfers(to_agent_id);
CREATE INDEX IF NOT EXISTS idx_transfer_status ON conversation_transfers(status);
CREATE INDEX IF NOT EXISTS idx_transfer_created_at ON conversation_transfers(created_at DESC);

COMMENT ON TABLE conversation_transfers IS 'History of conversation transfers between agents';
