-- ============================================================
-- V18: Alter penny_bots provider_type and persona_style to VARCHAR(50)
-- to fix Hibernate EnumType.STRING mapping error
-- ============================================================

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'penny_bots') THEN
        -- Alter provider_type to VARCHAR(50) if it is custom enum
        IF EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'penny_bots' AND column_name = 'provider_type' AND udt_name = 'llm_provider_type'
        ) THEN
            ALTER TABLE penny_bots ALTER COLUMN provider_type DROP DEFAULT;
            ALTER TABLE penny_bots ALTER COLUMN provider_type TYPE VARCHAR(50) USING provider_type::text;
            ALTER TABLE penny_bots ALTER COLUMN provider_type SET DEFAULT 'OPENAI';
        END IF;

        -- Alter persona_style to VARCHAR(50) if it is custom enum
        IF EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'penny_bots' AND column_name = 'persona_style' AND udt_name = 'bot_persona_style'
        ) THEN
            ALTER TABLE penny_bots ALTER COLUMN persona_style DROP DEFAULT;
            ALTER TABLE penny_bots ALTER COLUMN persona_style TYPE VARCHAR(50) USING persona_style::text;
            ALTER TABLE penny_bots ALTER COLUMN persona_style SET DEFAULT 'PROFESSIONAL';
        END IF;
    END IF;
END $$;
