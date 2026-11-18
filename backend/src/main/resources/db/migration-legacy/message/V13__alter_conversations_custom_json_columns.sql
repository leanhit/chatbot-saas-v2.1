-- ============================================================
-- V13: Alter conversations custom_attributes and custom_action_data to JSONB
-- ============================================================

-- Change conversations custom_attributes column from TEXT to JSONB
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'conversations' 
        AND column_name = 'custom_attributes' 
        AND data_type != 'jsonb'
    ) THEN
        ALTER TABLE conversations 
        ALTER COLUMN custom_attributes TYPE JSONB USING custom_attributes::JSONB;
    END IF;
END $$;

-- Change conversations custom_action_data column from TEXT to JSONB
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'conversations' 
        AND column_name = 'custom_action_data' 
        AND data_type != 'jsonb'
    ) THEN
        ALTER TABLE conversations 
        ALTER COLUMN custom_action_data TYPE JSONB USING custom_action_data::JSONB;
    END IF;
END $$;
