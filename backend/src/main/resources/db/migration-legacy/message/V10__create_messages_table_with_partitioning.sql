-- ============================================================
-- V10: Create messages table with time-based partitioning
-- ============================================================

-- Drop existing messages table if it exists (to recreate as partitioned)
-- This will lose existing data - consider data migration in production
DROP TABLE IF EXISTS messages CASCADE;

-- Create messages table with partitioning support
-- Using PostgreSQL declarative partitioning (available in PostgreSQL 10+)
CREATE TABLE messages (
    id BIGSERIAL,
    tenant_id BIGINT NOT NULL,
    conversation_id BIGINT NOT NULL,
    sender VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    raw_payload JSONB,
    message_type VARCHAR(50) NOT NULL,
    external_message_id VARCHAR(255),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    sent_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- BaseTenantEntity fields
    tenant_key VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (created_at);

-- Create indexes for messages
CREATE INDEX idx_messages_tenant ON messages(tenant_id);
CREATE INDEX idx_messages_tenant_conversation ON messages(tenant_id, conversation_id);
CREATE INDEX idx_messages_conversation ON messages(conversation_id);
CREATE INDEX idx_messages_created_at ON messages(created_at);
CREATE INDEX idx_messages_tenant_key ON messages(tenant_key);

-- Create partitions for current and future months (last 3 months + next 12 months)
-- This script creates partitions dynamically based on current date

-- Function to create monthly partitions
CREATE OR REPLACE FUNCTION create_monthly_partitions()
RETURNS void AS $$
DECLARE
    start_date DATE;
    end_date DATE;
    partition_name TEXT;
    i INTEGER;
BEGIN
    -- Create partitions for last 3 months
    FOR i IN -3..12 LOOP
        start_date := date_trunc('month', CURRENT_DATE + (i || ' months')::INTERVAL);
        end_date := start_date + INTERVAL '1 month';
        partition_name := 'messages_' || to_char(start_date, 'YYYY_MM');
        
        EXECUTE format('CREATE TABLE IF NOT EXISTS %I PARTITION OF messages FOR VALUES FROM (%L) TO (%L)',
                      partition_name, start_date, end_date);
        
        -- Create indexes on partition
        EXECUTE format('CREATE INDEX IF NOT EXISTS idx_%s_tenant ON %I(tenant_id)', 
                      partition_name, partition_name);
        EXECUTE format('CREATE INDEX IF NOT EXISTS idx_%s_conversation ON %I(conversation_id)', 
                      partition_name, partition_name);
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- Create initial partitions
SELECT create_monthly_partitions();

-- Create archive table for old messages
CREATE TABLE IF NOT EXISTS messages_archive (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    conversation_id BIGINT NOT NULL,
    sender VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    raw_payload JSONB,
    message_type VARCHAR(50) NOT NULL,
    external_message_id VARCHAR(255),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    sent_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    archived_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    archive_reason VARCHAR(100) DEFAULT 'retention_policy'
);

-- Create indexes for archive table
CREATE INDEX idx_messages_archive_tenant ON messages_archive(tenant_id);
CREATE INDEX idx_messages_archive_conversation ON messages_archive(conversation_id);
CREATE INDEX idx_messages_archive_created_at ON messages_archive(created_at);
CREATE INDEX idx_messages_archive_archived_at ON messages_archive(archived_at);

-- Create function to archive old messages (older than 90 days)
CREATE OR REPLACE FUNCTION archive_old_messages(days_to_keep INTEGER DEFAULT 90)
RETURNS INTEGER AS $$
DECLARE
    archived_count INTEGER;
BEGIN
    -- Move messages older than days_to_keep to archive table
    INSERT INTO messages_archive (id, tenant_id, conversation_id, sender, content, raw_payload, 
                                   message_type, external_message_id, is_read, sent_time, created_at, archived_at)
    SELECT id, tenant_id, conversation_id, sender, content, raw_payload, 
           message_type, external_message_id, is_read, sent_time, created_at, CURRENT_TIMESTAMP
    FROM messages
    WHERE created_at < CURRENT_DATE - (days_to_keep || ' days')::INTERVAL;
    
    GET DIAGNOSTICS archived_count = ROW_COUNT;
    
    -- Delete archived messages from main table
    DELETE FROM messages
    WHERE created_at < CURRENT_DATE - (days_to_keep || ' days')::INTERVAL;
    
    RETURN archived_count;
END;
$$ LANGUAGE plpgsql;

-- Create scheduled job to create new partitions monthly
-- This should be called by a scheduled job in the application
CREATE OR REPLACE FUNCTION ensure_future_partitions()
RETURNS void AS $$
BEGIN
    -- Ensure partitions exist for next 3 months
    PERFORM create_monthly_partitions();
END;
$$ LANGUAGE plpgsql;
