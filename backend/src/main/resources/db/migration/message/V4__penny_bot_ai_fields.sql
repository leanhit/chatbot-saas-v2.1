-- ============================================================
-- V10: Add AI/LLM fields to penny_bots table
-- ============================================================

DO $$
BEGIN
    -- Check if table exists first
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'penny_bots') THEN
        ALTER TABLE penny_bots ADD COLUMN IF NOT EXISTS system_prompt TEXT;
        ALTER TABLE penny_bots ADD COLUMN IF NOT EXISTS business_name VARCHAR(255);
        ALTER TABLE penny_bots ADD COLUMN IF NOT EXISTS business_description TEXT;
        ALTER TABLE penny_bots ADD COLUMN IF NOT EXISTS confidence_threshold FLOAT DEFAULT 0.6;
    END IF;
END $$;
