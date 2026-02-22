-- Create Messages Table
CREATE TABLE IF NOT EXISTS messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL,
    message_type VARCHAR(50) DEFAULT 'TEXT' CHECK (message_type IN ('TEXT', 'IMAGE', 'AUDIO', 'VIDEO', 'FILE', 'LOCATION', 'CONTACT')),
    content TEXT,
    sender_id UUID NOT NULL,
    sender_type VARCHAR(50) NOT NULL CHECK (sender_type IN ('USER', 'BOT', 'AGENT', 'SYSTEM')),
    recipient_id UUID,
    recipient_type VARCHAR(50) CHECK (recipient_type IN ('USER', 'BOT', 'AGENT', 'SYSTEM')),
    channel VARCHAR(50) NOT NULL CHECK (channel IN ('FACEBOOK', 'WEBSITE', 'MOBILE', 'EMAIL', 'SMS', 'WHATSAPP')),
    status VARCHAR(50) DEFAULT 'SENT' CHECK (status IN ('SENT', 'DELIVERED', 'READ', 'FAILED', 'DELETED')),
    metadata JSONB,
    external_message_id TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delivered_at TIMESTAMP,
    read_at TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_messages_conversation_id ON messages(conversation_id);
CREATE INDEX IF NOT EXISTS idx_messages_sender_id ON messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_messages_recipient_id ON messages(recipient_id);
CREATE INDEX IF NOT EXISTS idx_messages_channel ON messages(channel);
CREATE INDEX IF NOT EXISTS idx_messages_status ON messages(status);
CREATE INDEX IF NOT EXISTS idx_messages_created_at ON messages(created_at);
CREATE INDEX IF NOT EXISTS idx_messages_delivered_at ON messages(delivered_at);
CREATE INDEX IF NOT EXISTS idx_messages_read_at ON messages(read_at);

-- Create composite indexes for common queries
CREATE INDEX IF NOT EXISTS idx_messages_conversation_created ON messages(conversation_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_messages_sender_created ON messages(sender_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_messages_channel_created ON messages(channel, created_at DESC);

-- Create unique index for external message IDs per channel
CREATE UNIQUE INDEX IF NOT EXISTS idx_messages_external_id ON messages(external_message_id, channel) 
WHERE external_message_id IS NOT NULL;

-- Create GIN index for metadata JSONB
CREATE INDEX IF NOT EXISTS idx_messages_metadata ON messages USING GIN(metadata);

-- Create partial index for unread messages
CREATE INDEX IF NOT EXISTS idx_messages_unread ON messages(recipient_id, created_at) 
WHERE status != 'READ' AND recipient_id IS NOT NULL;

-- Function to update message status
CREATE OR REPLACE FUNCTION update_message_status(
    p_message_id UUID,
    p_new_status VARCHAR(50)
) RETURNS BOOLEAN AS $$
BEGIN
    UPDATE messages 
    SET status = p_new_status,
        delivered_at = CASE WHEN p_new_status = 'DELIVERED' THEN CURRENT_TIMESTAMP ELSE delivered_at END,
        read_at = CASE WHEN p_new_status = 'READ' THEN CURRENT_TIMESTAMP ELSE read_at END
    WHERE id = p_message_id;
    
    RETURN FOUND;
END;
$$ LANGUAGE plpgsql;
