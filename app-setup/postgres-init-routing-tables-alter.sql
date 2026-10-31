-- Phase 4A: Add team_id column to agent_workload if missing
-- This script adds the team_id column to the agent_workload table
-- Run this on the message database (chatbot_message_db)

-- Check if team_id column exists, if not add it
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'agent_workload' 
        AND column_name = 'team_id'
    ) THEN
        ALTER TABLE agent_workload ADD COLUMN team_id BIGINT;
        
        -- Add index for team_id if not exists
        CREATE INDEX IF NOT EXISTS idx_agent_workload_team ON agent_workload(team_id);
        
        RAISE NOTICE 'team_id column added to agent_workload table';
    ELSE
        RAISE NOTICE 'team_id column already exists in agent_workload table';
    END IF;
END $$;

-- Add foreign key constraint to teams table (optional - adjust based on your schema)
-- Uncomment if you have a teams table and want to enforce referential integrity
-- ALTER TABLE agent_workload 
-- ADD CONSTRAINT fk_agent_workload_team 
-- FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE SET NULL;

-- Comment on the column
COMMENT ON COLUMN agent_workload.team_id IS 'Team ID for the agent - used for team-based routing';
