-- Create Conversations Table
CREATE TABLE IF NOT EXISTS conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    participant_id UUID NOT NULL,
    participant_type VARCHAR(50) NOT NULL CHECK (participant_type IN ('USER', 'BOT', 'AGENT')),
    channel VARCHAR(50) NOT NULL CHECK (channel IN ('FACEBOOK', 'WEBSITE', 'MOBILE', 'EMAIL', 'SMS', 'WHATSAPP')),
    status VARCHAR(50) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'CLOSED', 'ARCHIVED', 'SUSPENDED')),
    last_message_at TIMESTAMP,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_conversations_participant_id ON conversations(participant_id);
CREATE INDEX IF NOT EXISTS idx_conversations_participant_type ON conversations(participant_type);
CREATE INDEX IF NOT EXISTS idx_conversations_channel ON conversations(channel);
CREATE INDEX IF NOT EXISTS idx_conversations_status ON conversations(status);
CREATE INDEX IF NOT EXISTS idx_conversations_last_message_at ON conversations(last_message_at);
CREATE INDEX IF NOT EXISTS idx_conversations_created_at ON conversations(created_at);

-- Create composite indexes for common queries
CREATE INDEX IF NOT EXISTS idx_conversations_participant_status ON conversations(participant_id, status);
CREATE INDEX IF NOT EXISTS idx_conversations_channel_status ON conversations(channel, status);
CREATE INDEX IF NOT EXISTS idx_conversations_active_recent ON conversations(status, last_message_at DESC) 
WHERE status = 'ACTIVE';

-- Create GIN index for metadata JSONB
CREATE INDEX IF NOT EXISTS idx_conversations_metadata ON conversations USING GIN(metadata);

-- Create trigger for updated_at
CREATE OR REPLACE FUNCTION update_conversations_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_conversations_updated_at 
    BEFORE UPDATE ON conversations 
    FOR EACH ROW 
    EXECUTE FUNCTION update_conversations_updated_at_column();

-- Function to update last_message_at
CREATE OR REPLACE FUNCTION update_conversation_last_message()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE conversations 
    SET last_message_at = NEW.created_at
    WHERE id = NEW.conversation_id;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to update last_message_at when new message is added
CREATE TRIGGER update_conversation_last_message_trigger
    AFTER INSERT ON messages
    FOR EACH ROW
    EXECUTE FUNCTION update_conversation_last_message();

-- Function to get conversation statistics
CREATE OR REPLACE FUNCTION get_conversation_stats(
    p_participant_id UUID,
    p_from_date TIMESTAMP DEFAULT NULL,
    p_to_date TIMESTAMP DEFAULT NULL
) RETURNS TABLE (
    total_conversations BIGINT,
    active_conversations BIGINT,
    closed_conversations BIGINT,
    total_messages BIGINT,
    average_messages_per_conversation DECIMAL(10,2)
) AS $$
BEGIN
    RETURN QUERY
    WITH conversation_stats AS (
        SELECT 
            c.id,
            c.status,
            COUNT(m.id) as message_count
        FROM conversations c
        LEFT JOIN messages m ON c.id = m.conversation_id
        WHERE c.participant_id = p_participant_id
            AND (p_from_date IS NULL OR c.created_at >= p_from_date)
            AND (p_to_date IS NULL OR c.created_at <= p_to_date)
        GROUP BY c.id, c.status
    )
    SELECT 
        COUNT(*) as total_conversations,
        COUNT(*) FILTER (WHERE status = 'ACTIVE') as active_conversations,
        COUNT(*) FILTER (WHERE status = 'CLOSED') as closed_conversations,
        SUM(message_count) as total_messages,
        CASE 
            WHEN COUNT(*) > 0 THEN ROUND(SUM(message_count)::DECIMAL / COUNT(*), 2)
            ELSE 0 
        END as average_messages_per_conversation
    FROM conversation_stats;
END;
$$ LANGUAGE plpgsql;
