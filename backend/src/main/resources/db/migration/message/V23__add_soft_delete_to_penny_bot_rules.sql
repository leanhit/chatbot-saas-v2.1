-- ============================================================
-- V23: Add soft delete columns to penny_bot_rules table
-- ============================================================

ALTER TABLE penny_bot_rules
    ADD COLUMN IF NOT EXISTS deleted_at  TIMESTAMP,
    ADD COLUMN IF NOT EXISTS deleted_by  VARCHAR(255);

-- Index để lọc nhanh các rule chưa bị xóa mềm
CREATE INDEX IF NOT EXISTS idx_bot_rules_active
    ON penny_bot_rules (bot_id, is_active)
    WHERE deleted_at IS NULL;

COMMENT ON COLUMN penny_bot_rules.deleted_at IS 'Soft-delete timestamp; NULL means the rule is not deleted';
COMMENT ON COLUMN penny_bot_rules.deleted_by IS 'Email/ID of the user who performed the soft delete';
