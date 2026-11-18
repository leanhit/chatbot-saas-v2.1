-- ============================================================
-- V14: Add AI Configuration v2.0 fields to penny_bots table
-- ============================================================

DO $$
BEGIN
    -- Check if table exists first
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'penny_bots') THEN
        -- Create enum types if they don't exist
        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'llm_provider_type') THEN
            CREATE TYPE llm_provider_type AS ENUM ('OPENAI', 'CLAUDE', 'GEMINI', 'OLLAMA');
        END IF;

        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'bot_persona_style') THEN
            CREATE TYPE bot_persona_style AS ENUM ('PROFESSIONAL', 'FRIENDLY', 'ENTHUSIASTIC', 'HUMOROUS', 'FORMAL');
        END IF;

        -- Add new columns for AI configuration
        ALTER TABLE penny_bots ADD COLUMN IF NOT EXISTS provider_type llm_provider_type DEFAULT 'OPENAI';
        ALTER TABLE penny_bots ADD COLUMN IF NOT EXISTS model_name VARCHAR(100) DEFAULT 'gpt-4o-mini';
        ALTER TABLE penny_bots ADD COLUMN IF NOT EXISTS temperature FLOAT DEFAULT 0.7;
        ALTER TABLE penny_bots ADD COLUMN IF NOT EXISTS persona_style bot_persona_style DEFAULT 'PROFESSIONAL';
        ALTER TABLE penny_bots ADD COLUMN IF NOT EXISTS custom_instructions TEXT;
        ALTER TABLE penny_bots ADD COLUMN IF NOT EXISTS greeting_message TEXT;
        ALTER TABLE penny_bots ADD COLUMN IF NOT EXISTS fallback_message TEXT;

        RAISE NOTICE 'Successfully added AI configuration v2.0 fields to penny_bots table';
    ELSE
        RAISE NOTICE 'penny_bots table does not exist, skipping migration';
    END IF;
END $$;
