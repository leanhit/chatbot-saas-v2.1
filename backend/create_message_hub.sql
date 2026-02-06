-- Create conversation_contexts table for Message Hub MVP
CREATE TABLE conversation_contexts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id VARCHAR(100) NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    last_intent VARCHAR(50),
    asked_price_count INTEGER NOT NULL DEFAULT 0,
    handler_type VARCHAR(20) NOT NULL DEFAULT 'BOT' CHECK (handler_type IN ('BOT', 'HUMAN')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX idx_conversation_contexts_conversation_id ON conversation_contexts(conversation_id);
CREATE INDEX idx_conversation_contexts_user_tenant ON conversation_contexts(user_id, tenant_id);
CREATE INDEX idx_conversation_contexts_handler_type ON conversation_contexts(handler_type);

-- Create trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_conversation_context_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER trigger_conversation_contexts_updated_at
    BEFORE UPDATE ON conversation_contexts
    FOR EACH ROW
    EXECUTE FUNCTION update_conversation_context_updated_at();
