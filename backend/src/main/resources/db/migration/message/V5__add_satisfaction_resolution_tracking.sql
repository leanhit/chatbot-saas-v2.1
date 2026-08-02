-- Add satisfaction and resolution tracking fields to conversations table
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'conversations' AND column_name = 'user_satisfaction_rating') THEN
        ALTER TABLE conversations ADD COLUMN user_satisfaction_rating INTEGER;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'conversations' AND column_name = 'resolution_status') THEN
        ALTER TABLE conversations ADD COLUMN resolution_status VARCHAR(50);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'conversations' AND column_name = 'resolution_time') THEN
        ALTER TABLE conversations ADD COLUMN resolution_time TIMESTAMP;
    END IF;
END $$;

-- Add indexes for performance (IF NOT EXISTS)
CREATE INDEX IF NOT EXISTS idx_conversation_resolution_status ON conversations(resolution_status);
CREATE INDEX IF NOT EXISTS idx_conversation_satisfaction_rating ON conversations(user_satisfaction_rating);

-- Add comments
COMMENT ON COLUMN conversations.user_satisfaction_rating IS 'User satisfaction rating from 1-5 based on feedback';
COMMENT ON COLUMN conversations.resolution_status IS 'Resolution status: resolved, unresolved, pending';
COMMENT ON COLUMN conversations.resolution_time IS 'Timestamp when conversation was resolved';
