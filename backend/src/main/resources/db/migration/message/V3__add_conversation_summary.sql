-- ============================================================
-- V10: Add summary column to conversations table
-- ============================================================

ALTER TABLE conversations
    ADD COLUMN IF NOT EXISTS summary TEXT;
