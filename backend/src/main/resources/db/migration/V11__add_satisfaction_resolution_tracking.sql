-- Add satisfaction and resolution tracking fields to conversations table
ALTER TABLE conversations ADD COLUMN user_satisfaction_rating INTEGER;
ALTER TABLE conversations ADD COLUMN resolution_status VARCHAR(50);
ALTER TABLE conversations ADD COLUMN resolution_time TIMESTAMP;

-- Add indexes for performance
CREATE INDEX idx_conversation_resolution_status ON conversations(resolution_status);
CREATE INDEX idx_conversation_satisfaction_rating ON conversations(user_satisfaction_rating);

-- Add comments
COMMENT ON COLUMN conversations.user_satisfaction_rating IS 'User satisfaction rating from 1-5 based on feedback';
COMMENT ON COLUMN conversations.resolution_status IS 'Resolution status: resolved, unresolved, pending';
COMMENT ON COLUMN conversations.resolution_time IS 'Timestamp when conversation was resolved';
