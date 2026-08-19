-- ============================================================
-- V0: Create base tables for message hub
-- ============================================================

-- Create conversations table
CREATE TABLE IF NOT EXISTS conversations (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    connection_id UUID,
    owner_id VARCHAR(255),
    external_user_id VARCHAR(255),
    user_name VARCHAR(255),
    user_avatar TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'open',
    channel VARCHAR(50),
    last_message_id BIGINT,
    agent_assigned_id BIGINT,
    is_closed_by_agent BOOLEAN NOT NULL DEFAULT FALSE,
    is_taken_over_by_agent BOOLEAN NOT NULL DEFAULT FALSE,
    tags TEXT,
    customer_tier VARCHAR(50),
    language VARCHAR(10),
    custom_attributes TEXT,
    queue_name VARCHAR(100),
    custom_action VARCHAR(100),
    custom_action_data TEXT,
    first_agent_response_time TIMESTAMP,
    first_bot_response_time TIMESTAMP,
    sla_breach_count INTEGER NOT NULL DEFAULT 0,
    expected_response_time BIGINT,
    required_skills TEXT,
    current_escalation_tier INTEGER,
    last_escalated_at TIMESTAMP,
    summary TEXT,
    user_satisfaction_rating INTEGER,
    resolution_status VARCHAR(50),
    resolution_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for conversations
CREATE INDEX IF NOT EXISTS idx_conversation_tenant ON conversations(tenant_id);
CREATE INDEX IF NOT EXISTS idx_conversation_tenant_connection ON conversations(tenant_id, connection_id);
CREATE INDEX IF NOT EXISTS idx_conversation_tenant_external_user ON conversations(tenant_id, external_user_id);
CREATE INDEX IF NOT EXISTS idx_conversation_status ON conversations(status);
CREATE INDEX IF NOT EXISTS idx_conversation_agent ON conversations(agent_assigned_id);

-- Create penny_bots table
CREATE TABLE IF NOT EXISTS penny_bots (
    id UUID PRIMARY KEY,
    bot_name VARCHAR(255) NOT NULL,
    bot_type VARCHAR(50) NOT NULL,
    tenant_id BIGINT NOT NULL,
    owner_id VARCHAR(255) NOT NULL,
    penny_bot_id VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP,
    configuration TEXT,
    system_prompt TEXT,
    business_name VARCHAR(255),
    business_description TEXT,
    confidence_threshold FLOAT DEFAULT 0.6
);

-- Create indexes for penny_bots
CREATE INDEX IF NOT EXISTS idx_penny_bots_tenant ON penny_bots(tenant_id);
CREATE INDEX IF NOT EXISTS idx_penny_bots_owner ON penny_bots(owner_id);
CREATE INDEX IF NOT EXISTS idx_penny_bots_active ON penny_bots(is_active, is_enabled);
