-- Seed Data for Chatbot SaaS Platform
-- This script inserts initial data for testing and development

-- Insert into Identity Hub
-- Note: This should be run against chatbot_identity_db

-- Default system roles (if roles table exists)
INSERT INTO system_roles (id, name, description, permissions) VALUES 
('role_admin', 'ADMIN', 'System Administrator', '{"all": true}'),
('role_user', 'USER', 'Regular User', '{"read": true, "write": true}'),
('role_viewer', 'VIEWER', 'Read-only User', '{"read": true}')
ON CONFLICT (id) DO NOTHING;

-- Sample users for testing
INSERT INTO users (id, email, password_hash, first_name, last_name, role, status, email_verified) VALUES 
('user_admin_001', 'admin@chatbot.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'System', 'Administrator', 'SUPER_ADMIN', TRUE),
('user_demo_001', 'john.doe@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'John', 'Doe', 'USER', 'ACTIVE', TRUE),
('user_demo_002', 'jane.smith@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Jane', 'Smith', 'USER', 'ACTIVE', TRUE),
('user_demo_003', 'bob.wilson@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Bob', 'Wilson', 'USER', 'ACTIVE', FALSE)
ON CONFLICT (email) DO NOTHING;

-- Insert into User Hub
-- Note: This should be run against chatbot_user_db

-- User profiles
INSERT INTO user_profiles (user_id, first_name, last_name, phone, timezone, language, bio) VALUES 
('user_admin_001', 'System', 'Administrator', '+1-555-0100', 'UTC', 'en', 'System administrator account'),
('user_demo_001', 'John', 'Doe', '+1-555-0101', 'America/New_York', 'en', 'Software developer passionate about chatbots'),
('user_demo_002', 'Jane', 'Smith', '+1-555-0102', 'America/Los_Angeles', 'en', 'Product manager with focus on user experience'),
('user_demo_003', 'Bob', 'Wilson', '+1-555-0103', 'Europe/London', 'en', 'Business analyst and data enthusiast')
ON CONFLICT (user_id) DO NOTHING;

-- User preferences
INSERT INTO user_preferences (user_id, email_notifications, sms_notifications, push_notifications, theme, currency, privacy_profile_visibility) VALUES 
('user_admin_001', TRUE, FALSE, TRUE, 'dark', 'USD', 'PUBLIC'),
('user_demo_001', TRUE, TRUE, TRUE, 'light', 'USD', 'PUBLIC'),
('user_demo_002', FALSE, FALSE, TRUE, 'auto', 'USD', 'PRIVATE'),
('user_demo_003', TRUE, FALSE, FALSE, 'light', 'EUR', 'PUBLIC')
ON CONFLICT (user_id) DO NOTHING;

-- User addresses
INSERT INTO user_addresses (user_id, address_type, street_address, city, state_province, postal_code, country, is_default) VALUES 
('user_demo_001', 'HOME', '123 Main St', 'New York', 'NY', '10001', 'USA', TRUE),
('user_demo_001', 'WORK', '456 Business Ave', 'New York', 'NY', '10002', 'USA', FALSE),
('user_demo_002', 'HOME', '789 Oak Blvd', 'Los Angeles', 'CA', '90001', 'USA', TRUE),
('user_demo_003', 'HOME', '321 Pine St', 'London', NULL, 'SW1A 0AA', 'UK', TRUE)
ON CONFLICT DO NOTHING;

-- Insert into Tenant Hub
-- Note: This should be run against chatbot_tenant_db

-- Sample tenants
INSERT INTO tenants (id, name, description, status, visibility, owner_id, industry, company_size, website) VALUES 
('tenant_demo_001', 'Acme Corporation', 'Technology company focused on customer service automation', 'ACTIVE', 'PRIVATE', 'user_demo_001', 'TECHNOLOGY', 'MEDIUM', 'https://acme.com'),
('tenant_demo_002', 'Global Retail Inc', 'Retail chain with multiple locations', 'ACTIVE', 'PUBLIC', 'user_demo_002', 'RETAIL', 'LARGE', 'https://globalretail.com'),
('tenant_demo_003', 'StartupXYZ', 'Early-stage startup building innovative products', 'ACTIVE', 'PRIVATE', 'user_demo_003', 'TECHNOLOGY', 'SMALL', 'https://startupxyz.com')
ON CONFLICT (id) DO NOTHING;

-- Tenant members
INSERT INTO tenant_members (tenant_id, user_id, role, status, invited_by) VALUES 
('tenant_demo_001', 'user_demo_001', 'OWNER', 'ACTIVE', 'user_demo_001'),
('tenant_demo_001', 'user_demo_002', 'ADMIN', 'ACTIVE', 'user_demo_001'),
('tenant_demo_002', 'user_demo_002', 'OWNER', 'ACTIVE', 'user_demo_002'),
('tenant_demo_002', 'user_demo_003', 'MEMBER', 'ACTIVE', 'user_demo_002'),
('tenant_demo_003', 'user_demo_003', 'OWNER', 'ACTIVE', 'user_demo_003')
ON CONFLICT (tenant_id, user_id) DO NOTHING;

-- Tenant profiles
INSERT INTO tenant_profiles (tenant_id, industry, company_size, website, timezone, currency, language) VALUES 
('tenant_demo_001', 'TECHNOLOGY', 'MEDIUM', 'https://acme.com', 'America/New_York', 'USD', 'en'),
('tenant_demo_002', 'RETAIL', 'LARGE', 'https://globalretail.com', 'America/Los_Angeles', 'USD', 'en'),
('tenant_demo_003', 'TECHNOLOGY', 'SMALL', 'https://startupxyz.com', 'Europe/London', 'GBP', 'en')
ON CONFLICT (tenant_id) DO NOTHING;

-- Insert into Wallet Hub
-- Note: This should be run against chatbot_wallet_db

-- Default wallets for users
INSERT INTO wallets (id, user_id, tenant_id, currency, balance, available_balance, status, wallet_type) VALUES 
('wallet_admin_001', 'user_admin_001', NULL, 'USD', 1000.00, 1000.00, 'ACTIVE', 'PERSONAL'),
('wallet_demo_001', 'user_demo_001', 'tenant_demo_001', 'USD', 500.00, 500.00, 'ACTIVE', 'BUSINESS'),
('wallet_demo_002', 'user_demo_002', 'tenant_demo_002', 'USD', 750.00, 750.00, 'ACTIVE', 'BUSINESS'),
('wallet_demo_003', 'user_demo_003', 'tenant_demo_003', 'EUR', 250.00, 250.00, 'ACTIVE', 'BUSINESS')
ON CONFLICT DO NOTHING;

-- Sample transactions
INSERT INTO transactions (id, wallet_id, transaction_type, amount, currency, status, description, fee, net_amount, external_transaction_id) VALUES 
('txn_topup_001', 'wallet_demo_001', 'TOPUP', 100.00, 'USD', 'COMPLETED', 'Initial wallet funding', 2.50, 97.50, 'ext_txn_001'),
('txn_purchase_001', 'wallet_demo_001', 'PURCHASE', 25.00, 'USD', 'COMPLETED', 'Premium subscription', 0.75, 25.75, 'ext_txn_002'),
('txn_topup_002', 'wallet_demo_002', 'TOPUP', 200.00, 'USD', 'COMPLETED', 'Monthly budget allocation', 5.00, 195.00, 'ext_txn_003')
ON CONFLICT (id) DO NOTHING;

-- Insert into App Hub
-- Note: This should be run against chatbot_app_db

-- Sample applications
INSERT INTO app_registry (id, name, description, category, app_type, status, version, developer, features, pricing) VALUES 
('app_facebook_001', 'Facebook Messenger', 'Connect your Facebook Messenger for customer support', 'SOCIAL', 'INTEGRATION', 'ACTIVE', '2.1.0', 'ChatBot Team', 
'["WEBHOOK_SUPPORT", "AUTO_REPLY", "MESSAGE_TEMPLATES", "ANALYTICS"]',
'{"type": "FREEMIUM", "trialDays": 14, "plans": [{"name": "FREE", "price": 0}, {"name": "PRO", "price": 29.99}]}'),

('app_botpress_001', 'Botpress Integration', 'Advanced AI chatbot integration with Botpress', 'AI', 'INTEGRATION', 'ACTIVE', '1.5.0', 'ChatBot Team',
'["NLP_SUPPORT", "INTENT_RECOGNITION", "DIALOG_FLOW", "MULTILINGUAL"]',
'{"type": "PAID", "trialDays": 30, "plans": [{"name": "STARTER", "price": 49.99}, {"name": "PRO", "price": 99.99}]}'),

('app_odoo_001', 'Odoo ERP', 'Connect with Odoo ERP system for business operations', 'ERP', 'INTEGRATION', 'ACTIVE', '1.2.0', 'ChatBot Team',
'["CUSTOMER_SYNC", "ORDER_MANAGEMENT", "INVOICE_SYNC", "INVENTORY_TRACKING"]',
'{"type": "ENTERPRISE", "trialDays": 0, "plans": [{"name": "BUSINESS", "price": 199.99}]}')
ON CONFLICT (id) DO NOTHING;

-- App subscriptions
INSERT INTO app_subscriptions (app_id, tenant_id, status, plan, configuration) VALUES 
('app_facebook_001', 'tenant_demo_001', 'ACTIVE', 'PRO', '{"PAGE_ACCESS_TOKEN": "masked_token", "WEBHOOK_SECRET": "masked_secret"}'),
('app_botpress_001', 'tenant_demo_001', 'ACTIVE', 'STARTER', '{"BOT_ID": "bot_001", "API_KEY": "masked_key"}'),
('app_facebook_001', 'tenant_demo_002', 'ACTIVE', 'FREE', '{"PAGE_ACCESS_TOKEN": "masked_token", "WEBHOOK_SECRET": "masked_secret"}'),
('app_odoo_001', 'tenant_demo_002', 'ACTIVE', 'BUSINESS', '{"URL": "https://odoo.globalretail.com", "API_KEY": "masked_key"}')
ON CONFLICT (app_id, tenant_id) DO NOTHING;

-- Insert into Config Hub
-- Note: This should be run against chatbot_config_db

-- Runtime configurations
INSERT INTO runtime_configs (config_key, config_value, config_type, scope, category, description) VALUES 
('chatbot.max_conversation_duration', '3600', 'INTEGER', 'GLOBAL', 'CHATBOT', 'Maximum conversation duration in seconds'),
('chatbot.welcome_message', 'Hello! How can I help you today?', 'STRING', 'TENANT', 'CHATBOT', 'Default welcome message'),
('chatbot.enable_analytics', 'true', 'BOOLEAN', 'GLOBAL', 'ANALYTICS', 'Enable analytics tracking'),
('chatbot.default_language', 'en', 'STRING', 'TENANT', 'LOCALIZATION', 'Default language for responses'),
('chatbot.file_upload.max_size', '10485760', 'INTEGER', 'GLOBAL', 'FILE_HANDLING', 'Maximum file upload size in bytes'),
('chatbot.rate_limit.requests_per_minute', '100', 'INTEGER', 'GLOBAL', 'SECURITY', 'Rate limit for API requests')
ON CONFLICT (config_key) DO NOTHING;

-- Environment configurations
INSERT INTO environment_configs (environment, config_key, config_value, config_type, description) VALUES 
('development', 'debug.enabled', 'true', 'BOOLEAN', 'Enable debug mode'),
('development', 'logging.level', 'DEBUG', 'STRING', 'Logging level for development'),
('production', 'debug.enabled', 'false', 'BOOLEAN', 'Disable debug mode in production'),
('production', 'logging.level', 'INFO', 'STRING', 'Logging level for production'),
('test', 'debug.enabled', 'true', 'BOOLEAN', 'Enable debug mode for testing'),
('test', 'logging.level', 'WARN', 'STRING', 'Minimal logging for tests')
ON CONFLICT (environment, config_key) DO NOTHING;

-- Insert into Message Hub
-- Note: This should be run against chatbot_message_db

-- Sample conversations
INSERT INTO conversations (id, participant_id, participant_type, channel, status, metadata) VALUES 
('conv_demo_001', 'user_demo_001', 'USER', 'FACEBOOK', 'ACTIVE', '{"source": "facebook_page_001"}'),
('conv_demo_002', 'user_demo_002', 'USER', 'WEBSITE', 'ACTIVE', '{"source": "website_chat"}'),
('conv_demo_003', 'user_demo_003', 'USER', 'MOBILE', 'CLOSED', '{"source": "mobile_app"}')
ON CONFLICT (id) DO NOTHING;

-- Sample messages
INSERT INTO messages (id, conversation_id, message_type, content, sender_id, sender_type, recipient_id, recipient_type, channel, status, metadata) VALUES 
('msg_demo_001', 'conv_demo_001', 'TEXT', 'Hello, I need help with my order', 'user_demo_001', 'USER', 'bot_facebook_001', 'BOT', 'FACEBOOK', 'DELIVERED', '{"platform": "facebook"}'),
('msg_demo_002', 'conv_demo_001', 'TEXT', 'I''d be happy to help you with your order. Can you please provide your order number?', 'bot_facebook_001', 'BOT', 'user_demo_001', 'USER', 'FACEBOOK', 'DELIVERED', '{"bot_response": true}'),
('msg_demo_003', 'conv_demo_002', 'TEXT', 'What are your pricing plans?', 'user_demo_002', 'USER', 'bot_website_001', 'BOT', 'WEBSITE', 'DELIVERED', '{"platform": "website"}'),
('msg_demo_004', 'conv_demo_002', 'TEXT', 'We offer flexible pricing plans starting from $29/month. Would you like me to send you our pricing brochure?', 'bot_website_001', 'BOT', 'user_demo_002', 'USER', 'WEBSITE', 'DELIVERED', '{"bot_response": true}')
ON CONFLICT (id) DO NOTHING;

-- Update conversation timestamps
UPDATE conversations SET last_message_at = NOW() WHERE id IN ('conv_demo_001', 'conv_demo_002');

-- Insert routing rules
INSERT INTO routing_rules (tenant_id, name, priority, conditions, actions, status) VALUES 
('tenant_demo_001', 'Customer Support Routing', 1, 
'[{"field": "message.content", "operator": "CONTAINS", "value": "order", "caseSensitive": false}]',
'[{"type": "ROUTE_TO", "destination": {"type": "BOTPRESS", "botId": "support_bot"}}]',
'ACTIVE'),

('tenant_demo_002', 'Sales Inquiry Routing', 2,
'[{"field": "message.content", "operator": "CONTAINS", "value": "pricing", "caseSensitive": false}]',
'[{"type": "ROUTE_TO", "destination": {"type": "HUMAN_AGENT", "queueId": "sales_queue"}}]',
'ACTIVE'),

('tenant_demo_003', 'Technical Support', 1,
'[{"field": "message.content", "operator": "CONTAINS", "value": "technical", "caseSensitive": false}]',
'[{"type": "ROUTE_TO", "destination": {"type": "BOTPRESS", "botId": "tech_support_bot"}}]',
'ACTIVE')
ON CONFLICT DO NOTHING;

-- Insert into Spokes Database
-- Note: This should be run against chatbot_spokes_db

-- Facebook connections
INSERT INTO facebook_connections (id, tenant_id, page_id, page_name, connection_status, metadata) VALUES 
('fb_conn_001', 'tenant_demo_001', '123456789012345', 'Acme Corp Customer Support', 'ACTIVE', '{"webhook_url": "https://chatbot.com/webhooks/fb/123456789012345"}'),
('fb_conn_002', 'tenant_demo_002', '987654321098765', 'Global Retail', 'ACTIVE', '{"webhook_url": "https://chatbot.com/webhooks/fb/987654321098765"}')
ON CONFLICT (id) DO NOTHING;

-- Facebook users
INSERT INTO facebook_users (id, facebook_user_id, user_id, tenant_id, first_name, last_name, locale, timezone) VALUES 
('fb_user_001', 'fb_user_123456789', 'user_demo_001', 'tenant_demo_001', 'John', 'Doe', 'en_US', -5),
('fb_user_002', 'fb_user_987654321', 'user_demo_002', 'tenant_demo_002', 'Jane', 'Smith', 'en_US', -8)
ON CONFLICT (facebook_user_id) DO NOTHING;

-- Botpress user mappings
INSERT INTO botpress_user_mappings (id, tenant_id, platform_user_id, platform_type, botpress_user_id) VALUES 
('bp_map_001', 'tenant_demo_001', 'fb_user_123456789', 'FACEBOOK', 'bp_user_001'),
('bp_map_002', 'tenant_demo_001', 'user_demo_001', 'WEBSITE', 'bp_user_002'),
('bp_map_003', 'tenant_demo_002', 'fb_user_987654321', 'FACEBOOK', 'bp_user_003')
ON CONFLICT (tenant_id, platform_user_id, platform_type) DO NOTHING;

-- Output summary
DO $$
BEGIN
    RAISE NOTICE '🌱 Seed data insertion completed!';
    RAISE NOTICE '📊 Summary:';
    RAISE NOTICE '  - Users: 4 (including 1 admin)';
    RAISE NOTICE '  - Tenants: 3';
    RAISE NOTICE '  - Wallets: 4';
    RAISE NOTICE '  - Applications: 3';
    RAISE NOTICE '  - Conversations: 3';
    RAISE NOTICE '  - Messages: 4';
    RAISE NOTICE '  - Facebook Connections: 2';
    RAISE NOTICE '';
    RAISE NOTICE '🔑 Default Login:';
    RAISE NOTICE '  Email: admin@chatbot.com';
    RAISE NOTICE '  Password: admin123';
    RAISE NOTICE '';
END $$;
