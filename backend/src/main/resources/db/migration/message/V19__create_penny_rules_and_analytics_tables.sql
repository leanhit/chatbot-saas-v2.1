-- ============================================================
-- V19: Create penny_bot_rules, penny_response_templates, and penny_analytics_events tables
-- ============================================================

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'penny_bots') THEN
        -- Create penny_bot_rules table
        CREATE TABLE IF NOT EXISTS penny_bot_rules (
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            bot_id UUID NOT NULL,
            name VARCHAR(255) NOT NULL,
            description TEXT,
            condition TEXT NOT NULL,
            action TEXT NOT NULL,
            priority INTEGER NOT NULL DEFAULT 0,
            is_active BOOLEAN NOT NULL DEFAULT true,
            rule_type VARCHAR(50) NOT NULL DEFAULT 'RESPONSE',
            trigger_type VARCHAR(50) NOT NULL DEFAULT 'INTENT',
            trigger_value VARCHAR(255),
            created_at TIMESTAMP NOT NULL DEFAULT NOW(),
            updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
            created_by VARCHAR(255) NOT NULL,
            updated_by VARCHAR(255),
            execution_count BIGINT DEFAULT 0,
            last_executed_at TIMESTAMP,
            metadata TEXT,

            CONSTRAINT fk_rule_bot FOREIGN KEY (bot_id) REFERENCES penny_bots(id) ON DELETE CASCADE
        );

        CREATE INDEX IF NOT EXISTS idx_bot_rules_bot ON penny_bot_rules(bot_id);
        CREATE INDEX IF NOT EXISTS idx_bot_rules_trigger ON penny_bot_rules(bot_id, trigger_type, trigger_value);

        -- Create penny_response_templates table
        CREATE TABLE IF NOT EXISTS penny_response_templates (
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            bot_id UUID NOT NULL,
            name VARCHAR(255) NOT NULL,
            description TEXT,
            intent VARCHAR(255) NOT NULL,
            template_text TEXT NOT NULL,
            template_type VARCHAR(50) NOT NULL DEFAULT 'TEXT',
            language VARCHAR(10) NOT NULL DEFAULT 'vi',
            is_active BOOLEAN NOT NULL DEFAULT true,
            priority INTEGER NOT NULL DEFAULT 0,
            variables TEXT,
            quick_replies TEXT,
            attachments TEXT,
            created_at TIMESTAMP NOT NULL DEFAULT NOW(),
            updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
            created_by VARCHAR(255) NOT NULL,
            updated_by VARCHAR(255),
            usage_count BIGINT DEFAULT 0,
            last_used_at TIMESTAMP,

            CONSTRAINT fk_template_bot FOREIGN KEY (bot_id) REFERENCES penny_bots(id) ON DELETE CASCADE
        );

        CREATE INDEX IF NOT EXISTS idx_response_templates_bot ON penny_response_templates(bot_id);
        CREATE INDEX IF NOT EXISTS idx_response_templates_intent ON penny_response_templates(bot_id, intent);

        -- Create penny_analytics_events table
        CREATE TABLE IF NOT EXISTS penny_analytics_events (
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            bot_id UUID,
            event_type VARCHAR(100) NOT NULL,
            timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
            data JSONB,
            user_id VARCHAR(255),
            platform VARCHAR(100),
            intent VARCHAR(255),
            provider_used VARCHAR(100),
            processing_time_ms BIGINT,
            has_error BOOLEAN,
            error_type VARCHAR(255),
            created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
        );

        CREATE INDEX IF NOT EXISTS idx_analytics_bot ON penny_analytics_events(bot_id);
        CREATE INDEX IF NOT EXISTS idx_analytics_event_type ON penny_analytics_events(event_type);
        CREATE INDEX IF NOT EXISTS idx_analytics_timestamp ON penny_analytics_events(timestamp);
        CREATE INDEX IF NOT EXISTS idx_analytics_bot_timestamp ON penny_analytics_events(bot_id, timestamp);

        RAISE NOTICE 'Successfully created penny_bot_rules, penny_response_templates, and penny_analytics_events tables';
    ELSE
        RAISE NOTICE 'penny_bots table does not exist, skipping rules and analytics tables creation';
    END IF;
END $$;
