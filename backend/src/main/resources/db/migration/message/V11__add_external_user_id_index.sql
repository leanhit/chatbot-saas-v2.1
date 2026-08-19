-- ============================================================
-- V11: Add index for external_user_id in conversations table
-- ============================================================

-- Add single-column index for external_user_id to optimize queries
-- that filter by external_user_id without tenant_id
CREATE INDEX IF NOT EXISTS idx_conversation_external_user ON conversations(external_user_id);
