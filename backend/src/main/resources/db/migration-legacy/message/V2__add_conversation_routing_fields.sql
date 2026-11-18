-- ============================================================
-- V9: Add Conversation Routing & Escalation Tracking Fields
-- Phase 2.2: Multi-tier Escalation tracking
-- Phase 3.2: Skills-based Routing
-- ============================================================

-- Add required_skills: JSON array of skill strings needed for this conversation
-- Used by AgentAssignmentService for skills-based routing (Phase 3.2)
ALTER TABLE conversations
    ADD COLUMN IF NOT EXISTS required_skills TEXT;

-- Add current_escalation_tier: tracks which escalation tier the conversation is at
-- NULL = not escalated yet, 1 = Agent tier, 2 = Team Lead tier, 3 = Supervisor tier
ALTER TABLE conversations
    ADD COLUMN IF NOT EXISTS current_escalation_tier INTEGER;

-- Add last_escalated_at: timestamp of the most recent escalation
-- Used by EscalationService to determine if re-escalation is needed
ALTER TABLE conversations
    ADD COLUMN IF NOT EXISTS last_escalated_at TIMESTAMP;

-- Index for querying escalated conversations by tier
CREATE INDEX IF NOT EXISTS idx_conversation_escalation_tier
    ON conversations (tenant_id, current_escalation_tier)
    WHERE current_escalation_tier IS NOT NULL;
