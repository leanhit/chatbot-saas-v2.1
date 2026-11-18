-- ============================================================
-- V8: Alter columns to JSONB type
-- ============================================================

-- Change routing_rules columns from TEXT to JSONB
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'routing_rules' 
        AND column_name = 'conditions' 
        AND data_type = 'text'
    ) THEN
        ALTER TABLE routing_rules 
        ALTER COLUMN conditions TYPE JSONB USING conditions::JSONB;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'routing_rules' 
        AND column_name = 'action' 
        AND data_type = 'text'
    ) THEN
        ALTER TABLE routing_rules 
        ALTER COLUMN action TYPE JSONB USING action::JSONB;
    END IF;
END $$;

-- Change agents skills column from TEXT to JSONB
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'agents' 
        AND column_name = 'skills' 
        AND data_type = 'text'
    ) THEN
        ALTER TABLE agents 
        ALTER COLUMN skills TYPE JSONB USING skills::JSONB;
    END IF;
END $$;

-- Change agents assignment_preferences column from TEXT to JSONB
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'agents' 
        AND column_name = 'assignment_preferences' 
        AND data_type = 'text'
    ) THEN
        ALTER TABLE agents 
        ALTER COLUMN assignment_preferences TYPE JSONB USING assignment_preferences::JSONB;
    END IF;
END $$;

-- Change conversations required_skills column from TEXT to JSONB
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'conversations' 
        AND column_name = 'required_skills' 
        AND data_type = 'text'
    ) THEN
        ALTER TABLE conversations 
        ALTER COLUMN required_skills TYPE JSONB USING required_skills::JSONB;
    END IF;
END $$;

-- Change conversations tags column from TEXT to JSONB
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'conversations' 
        AND column_name = 'tags' 
        AND data_type = 'text'
    ) THEN
        ALTER TABLE conversations 
        ALTER COLUMN tags TYPE JSONB USING tags::JSONB;
    END IF;
END $$;
