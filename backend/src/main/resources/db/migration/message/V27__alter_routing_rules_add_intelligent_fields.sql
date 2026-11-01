-- ============================================================
-- V27: Add Intelligent Routing fields to routing_rules table
-- ============================================================

-- Add target_type column
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'routing_rules' 
        AND column_name = 'target_type'
    ) THEN
        ALTER TABLE routing_rules 
        ADD COLUMN target_type VARCHAR(50) NOT NULL DEFAULT 'AGENT';
    END IF;
END $$;

-- Add target_agent_id column
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'routing_rules' 
        AND column_name = 'target_agent_id'
    ) THEN
        ALTER TABLE routing_rules 
        ADD COLUMN target_agent_id BIGINT;
    END IF;
END $$;

-- Add target_team_id column
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'routing_rules' 
        AND column_name = 'target_team_id'
    ) THEN
        ALTER TABLE routing_rules 
        ADD COLUMN target_team_id BIGINT;
    END IF;
END $$;

-- Add target_skill_id column
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'routing_rules' 
        AND column_name = 'target_skill_id'
    ) THEN
        ALTER TABLE routing_rules 
        ADD COLUMN target_skill_id BIGINT;
    END IF;
END $$;

-- Add max_concurrent_conversations column
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'routing_rules' 
        AND column_name = 'max_concurrent_conversations'
    ) THEN
        ALTER TABLE routing_rules 
        ADD COLUMN max_concurrent_conversations INTEGER;
    END IF;
END $$;

-- Rename active to is_active if needed
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'routing_rules' 
        AND column_name = 'active'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'routing_rules' 
        AND column_name = 'is_active'
    ) THEN
        ALTER TABLE routing_rules 
        RENAME COLUMN active TO is_active;
    END IF;
END $$;

-- Add is_default column
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'routing_rules' 
        AND column_name = 'is_default'
    ) THEN
        ALTER TABLE routing_rules 
        ADD COLUMN is_default BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;
END $$;

-- Add check constraints
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE table_name = 'routing_rules' 
        AND constraint_name = 'chk_routing_rule_target_type'
    ) THEN
        ALTER TABLE routing_rules 
        ADD CONSTRAINT chk_routing_rule_target_type 
        CHECK (target_type IN ('AGENT', 'TEAM', 'SKILL', 'QUEUE'));
    END IF;
END $$;

-- Add indexes
CREATE INDEX IF NOT EXISTS idx_routing_rule_target_type ON routing_rules(target_type);
CREATE INDEX IF NOT EXISTS idx_routing_rule_target_agent ON routing_rules(target_agent_id);
CREATE INDEX IF NOT EXISTS idx_routing_rule_target_team ON routing_rules(target_team_id);
CREATE INDEX IF NOT EXISTS idx_routing_rule_default ON routing_rules(tenant_id, is_default) WHERE is_default = TRUE;

COMMENT ON COLUMN routing_rules.target_type IS 'Target type for routing: AGENT, TEAM, SKILL, or QUEUE';
COMMENT ON COLUMN routing_rules.target_agent_id IS 'Specific agent ID to route to';
COMMENT ON COLUMN routing_rules.target_team_id IS 'Team ID to route to';
COMMENT ON COLUMN routing_rules.target_skill_id IS 'Skill ID for skill-based routing';
COMMENT ON COLUMN routing_rules.max_concurrent_conversations IS 'Max concurrent conversations for this rule';
COMMENT ON COLUMN routing_rules.is_default IS 'Whether this is the default fallback rule';
