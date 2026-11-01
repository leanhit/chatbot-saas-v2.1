-- Phase C: Intelligent Routing Tables
-- Create tables for AgentWorkload, TeamWorkload, and RoutingHistory
-- Run this script on the message database (chatbot_message_db)

-- Create agent_workload table
CREATE TABLE IF NOT EXISTS agent_workload (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    agent_id BIGINT NOT NULL,
    team_id BIGINT,
    active_conversations INTEGER NOT NULL DEFAULT 0,
    max_concurrent_conversations INTEGER NOT NULL DEFAULT 10,
    total_conversations_today INTEGER NOT NULL DEFAULT 0,
    total_conversations_this_week INTEGER NOT NULL DEFAULT 0,
    average_response_time_seconds INTEGER,
    last_activity_at TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    priority_score INTEGER NOT NULL DEFAULT 50,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_agent_workload_conversations CHECK (active_conversations >= 0),
    CONSTRAINT chk_agent_workload_max_concurrent CHECK (max_concurrent_conversations > 0),
    CONSTRAINT chk_agent_workload_status CHECK (status IN ('AVAILABLE', 'BUSY', 'OFFLINE', 'AWAY'))
);

-- Create indexes for agent_workload
CREATE INDEX IF NOT EXISTS idx_agent_workload_tenant ON agent_workload(tenant_id);
CREATE INDEX IF NOT EXISTS idx_agent_workload_agent ON agent_workload(agent_id);
CREATE INDEX IF NOT EXISTS idx_agent_workload_team ON agent_workload(team_id);
CREATE INDEX IF NOT EXISTS idx_agent_workload_status ON agent_workload(status);
CREATE INDEX IF NOT EXISTS idx_agent_workload_tenant_status ON agent_workload(tenant_id, status);

-- Create team_workload table
CREATE TABLE IF NOT EXISTS team_workload (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    team_id BIGINT NOT NULL,
    active_conversations INTEGER NOT NULL DEFAULT 0,
    max_concurrent_conversations INTEGER NOT NULL DEFAULT 50,
    total_conversations_today INTEGER NOT NULL DEFAULT 0,
    total_conversations_this_week INTEGER NOT NULL DEFAULT 0,
    average_response_time_seconds INTEGER,
    last_activity_at TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_team_workload_conversations CHECK (active_conversations >= 0),
    CONSTRAINT chk_team_workload_max_concurrent CHECK (max_concurrent_conversations > 0),
    CONSTRAINT chk_team_workload_status CHECK (status IN ('AVAILABLE', 'BUSY', 'OFFLINE', 'AWAY'))
);

-- Create indexes for team_workload
CREATE INDEX IF NOT EXISTS idx_team_workload_tenant ON team_workload(tenant_id);
CREATE INDEX IF NOT EXISTS idx_team_workload_team ON team_workload(team_id);
CREATE INDEX IF NOT EXISTS idx_team_workload_status ON team_workload(status);
CREATE INDEX IF NOT EXISTS idx_team_workload_tenant_status ON team_workload(tenant_id, status);

-- Create routing_history table
CREATE TABLE IF NOT EXISTS routing_history (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    conversation_id BIGINT NOT NULL,
    assigned_agent_id BIGINT,
    assigned_team_id BIGINT,
    routing_rule_id BIGINT,
    rule_name VARCHAR(255),
    rule_type VARCHAR(50),
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reason TEXT,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for routing_history
CREATE INDEX IF NOT EXISTS idx_routing_history_tenant ON routing_history(tenant_id);
CREATE INDEX IF NOT EXISTS idx_routing_history_conversation ON routing_history(conversation_id);
CREATE INDEX IF NOT EXISTS idx_routing_history_agent ON routing_history(assigned_agent_id);
CREATE INDEX IF NOT EXISTS idx_routing_history_team ON routing_history(assigned_team_id);
CREATE INDEX IF NOT EXISTS idx_routing_history_rule ON routing_history(routing_rule_id);
CREATE INDEX IF NOT EXISTS idx_routing_history_tenant_conversation ON routing_history(tenant_id, conversation_id);
CREATE INDEX IF NOT EXISTS idx_routing_history_assigned_at ON routing_history(assigned_at DESC);

-- Add comment to tables
COMMENT ON TABLE agent_workload IS 'Tracks agent workload, availability, and capacity for intelligent routing';
COMMENT ON TABLE team_workload IS 'Tracks team workload and capacity for intelligent routing';
COMMENT ON TABLE routing_history IS 'History of routing decisions for audit and analysis';

-- Grant permissions (adjust based on your database user)
-- GRANT SELECT, INSERT, UPDATE, DELETE ON agent_workload TO chatbot_user;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON team_workload TO chatbot_user;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON routing_history TO chatbot_user;
