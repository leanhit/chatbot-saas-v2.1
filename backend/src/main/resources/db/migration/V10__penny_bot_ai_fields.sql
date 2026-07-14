-- ============================================================
-- V10: Add AI/LLM fields to penny_bots table
-- ============================================================

ALTER TABLE penny_bots ADD COLUMN IF NOT EXISTS system_prompt TEXT;
ALTER TABLE penny_bots ADD COLUMN IF NOT EXISTS business_name VARCHAR(255);
ALTER TABLE penny_bots ADD COLUMN IF NOT EXISTS business_description TEXT;
ALTER TABLE penny_bots ADD COLUMN IF NOT EXISTS confidence_threshold FLOAT DEFAULT 0.6;
