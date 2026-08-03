-- ============================================================
-- V8: Create packages table and insert default packages
-- ============================================================

-- Create packages table
CREATE TABLE IF NOT EXISTS packages (
    id BIGSERIAL PRIMARY KEY,
    package_id VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(10) NOT NULL DEFAULT 'VND',
    duration VARCHAR(50) NOT NULL,
    description TEXT,
    message_limit INTEGER NOT NULL,
    chatbot_limit INTEGER NOT NULL,
    has_priority_support BOOLEAN NOT NULL DEFAULT FALSE,
    has_analytics BOOLEAN NOT NULL DEFAULT FALSE,
    has_advanced_analytics BOOLEAN NOT NULL DEFAULT FALSE,
    has_custom_integrations BOOLEAN NOT NULL DEFAULT FALSE,
    has_dedicated_support BOOLEAN NOT NULL DEFAULT FALSE,
    has_custom_features BOOLEAN NOT NULL DEFAULT FALSE,
    has_sla_guarantee BOOLEAN NOT NULL DEFAULT FALSE,
    isactive BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL,
    badge VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_packages_package_id ON packages(package_id);
CREATE INDEX IF NOT EXISTS idx_packages_isactive ON packages(isactive);
CREATE INDEX IF NOT EXISTS idx_packages_sort_order ON packages(sort_order);

-- Insert default packages
INSERT INTO packages (package_id, name, price, currency, duration, description, message_limit, chatbot_limit, 
                      has_priority_support, has_analytics, has_advanced_analytics, has_custom_integrations, 
                      has_dedicated_support, has_custom_features, has_sla_guarantee, isactive, sort_order, badge)
VALUES 
    ('free', 'Free Plan', 0.00, 'VND', '1 month', 'Basic plan for individuals', 1000, 1, 
     FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE, 1, NULL),
    ('pro', 'Pro Plan', 500000.00, 'VND', '1 month', 'Professional plan for small teams', 10000, 5, 
     TRUE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE, 2, 'POPULAR'),
    ('business', 'Business Plan', 2000000.00, 'VND', '1 month', 'Business plan for growing companies', 50000, 20, 
     TRUE, TRUE, TRUE, TRUE, FALSE, FALSE, TRUE, TRUE, 3, 'RECOMMENDED'),
    ('enterprise', 'Enterprise Plan', 10000000.00, 'VND', '1 year', 'Enterprise plan with full features', -1, -1, 
     TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, 4, NULL)
ON CONFLICT (package_id) DO NOTHING;

-- Initialize existing tenants with free package if tenants table exists in this database
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'tenants') THEN
        UPDATE tenants 
        SET current_package_id = 'free',
            package_activated_at = CURRENT_TIMESTAMP
        WHERE current_package_id IS NULL;
        
        RAISE NOTICE 'Initialized existing tenants with free package';
    END IF;
END $$;

