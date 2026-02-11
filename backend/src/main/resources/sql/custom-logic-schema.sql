-- Penny Custom Logic Schema
-- Schema cho tính năng user tự thêm/sửa logic trả lời của bot

-- Table cho Bot Rules
CREATE TABLE IF NOT EXISTS penny_bot_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bot_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    condition TEXT NOT NULL,
    action TEXT NOT NULL,
    priority INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    rule_type VARCHAR(50) NOT NULL DEFAULT 'RESPONSE',
    trigger_type VARCHAR(50) NOT NULL DEFAULT 'INTENT',
    trigger_value VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255),
    execution_count BIGINT DEFAULT 0,
    last_executed_at TIMESTAMP,
    metadata TEXT
);

-- Table cho Response Templates
CREATE TABLE IF NOT EXISTS penny_response_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bot_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    intent VARCHAR(255) NOT NULL,
    template_text TEXT NOT NULL,
    template_type VARCHAR(50) NOT NULL DEFAULT 'TEXT',
    language VARCHAR(10) DEFAULT 'vi',
    is_active BOOLEAN DEFAULT true,
    priority INTEGER DEFAULT 0,
    variables TEXT,
    quick_replies TEXT,
    attachments TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255),
    usage_count BIGINT DEFAULT 0,
    last_used_at TIMESTAMP
);

-- Indexes cho performance
CREATE INDEX IF NOT EXISTS idx_penny_bot_rules_bot_id ON penny_bot_rules(bot_id);
CREATE INDEX IF NOT EXISTS idx_penny_bot_rules_active ON penny_bot_rules(is_active);
CREATE INDEX IF NOT EXISTS idx_penny_bot_rules_trigger ON penny_bot_rules(trigger_type, trigger_value);
CREATE INDEX IF NOT EXISTS idx_penny_bot_rules_priority ON penny_bot_rules(priority DESC);

CREATE INDEX IF NOT EXISTS idx_penny_response_templates_bot_id ON penny_response_templates(bot_id);
CREATE INDEX IF NOT EXISTS idx_penny_response_templates_active ON penny_response_templates(is_active);
CREATE INDEX IF NOT EXISTS idx_penny_response_templates_intent ON penny_response_templates(intent);
CREATE INDEX IF NOT EXISTS idx_penny_response_templates_language ON penny_response_templates(language);
CREATE INDEX IF NOT EXISTS idx_penny_response_templates_priority ON penny_response_templates(priority DESC);

-- Unique constraints
CREATE UNIQUE INDEX IF NOT EXISTS idx_penny_bot_rules_unique_name 
ON penny_bot_rules(bot_id, name) WHERE is_active = true;

CREATE UNIQUE INDEX IF NOT EXISTS idx_penny_response_templates_unique_name 
ON penny_response_templates(bot_id, name) WHERE is_active = true;

-- Comments cho documentation
COMMENT ON TABLE penny_bot_rules IS 'Rules tùy chỉnh cho bot behavior';
COMMENT ON COLUMN penny_bot_rules.rule_type IS 'Loại rule: RESPONSE, REDIRECT, WEBHOOK, SCRIPT';
COMMENT ON COLUMN penny_bot_rules.trigger_type IS 'Loại trigger: INTENT, KEYWORD, REGEX, CONDITION, ALWAYS';
COMMENT ON COLUMN penny_bot_rules.condition IS 'Điều kiện trigger (JSON expression)';
COMMENT ON COLUMN penny_bot_rules.action IS 'Hành động khi match (JSON response template)';

COMMENT ON TABLE penny_response_templates IS 'Templates phản hồi tùy chỉnh';
COMMENT ON COLUMN penny_response_templates.template_type IS 'Loại template: TEXT, RICH_TEXT, CARD, LIST, CAROUSEL, FORM, CUSTOM';
COMMENT ON COLUMN penny_response_templates.template_text IS 'Nội dung template với variables {{variable_name}}';
COMMENT ON COLUMN penny_response_templates.variables IS 'JSON định nghĩa variables cho template';
COMMENT ON COLUMN penny_response_templates.quick_replies IS 'JSON array của quick replies';
COMMENT ON COLUMN penny_response_templates.attachments IS 'JSON array của attachments';
