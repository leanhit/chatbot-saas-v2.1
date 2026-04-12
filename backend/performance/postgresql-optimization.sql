-- PostgreSQL Performance Optimization for Chatbot SaaS v2.1
-- Run this script on all PostgreSQL databases

-- ========================================
-- 1. Memory Configuration
-- ========================================

-- Set shared buffers (25% of RAM, adjust based on available memory)
ALTER SYSTEM SET shared_buffers = '256MB';

-- Set effective cache size (75% of RAM)
ALTER SYSTEM SET effective_cache_size = '1GB';

-- Set work memory for complex queries
ALTER SYSTEM SET work_mem = '4MB';

-- Set maintenance work memory for maintenance operations
ALTER SYSTEM SET maintenance_work_mem = '64MB';

-- ========================================
-- 2. Connection Configuration
-- ========================================

-- Set maximum connections
ALTER SYSTEM SET max_connections = '200';

-- Set shared_preload_libraries for monitoring
ALTER SYSTEM SET shared_preload_libraries = 'pg_stat_statements';

-- ========================================
-- 3. WAL Configuration
-- ========================================

-- Set WAL size
ALTER SYSTEM SET wal_buffers = '16MB';

-- Set checkpoint completion target
ALTER SYSTEM SET checkpoint_completion_target = '0.9';

-- Set WAL writer delay
ALTER SYSTEM SET wal_writer_delay = '200ms';

-- ========================================
-- 4. Query Performance
-- ========================================

-- Enable statement statistics
ALTER SYSTEM SET track_activity_query_size = '2048';

-- Track all statements
ALTER SYSTEM SET pg_stat_statements.track = 'all';

-- Track utility statements
ALTER SYSTEM SET pg_stat_statements.track_utility = 'true';

-- Set max statement age
ALTER SYSTEM SET pg_stat_statements.max = '10000';

-- ========================================
-- 5. Autovacuum Configuration
-- ========================================

-- Set autovacuum threshold
ALTER SYSTEM SET autovacuum_vacuum_threshold = '50';

-- Set autovacuum scale factor
ALTER SYSTEM SET autovacuum_vacuum_scale_factor = '0.1';

-- Set autovacuum analyze threshold
ALTER SYSTEM SET autovacuum_analyze_threshold = '50';

-- Set autovacuum analyze scale factor
ALTER SYSTEM SET autovacuum_analyze_scale_factor = '0.05';

-- ========================================
-- 6. Logging Configuration
-- ========================================

-- Log slow queries
ALTER SYSTEM SET log_min_duration_statement = '1000';

-- Log checkpoints
ALTER SYSTEM SET log_checkpoints = 'on';

-- Log connections
ALTER SYSTEM SET log_connections = 'on';

-- Log disconnections
ALTER SYSTEM SET log_disconnections = 'on';

-- Log lock waits
ALTER SYSTEM SET log_lock_waits = 'on';

-- ========================================
-- 7. Reload Configuration
-- ========================================

SELECT pg_reload_conf();

-- ========================================
-- 8. Create Performance Indexes
-- ========================================

-- Users table indexes
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_tenant_id ON users(tenant_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_created_at ON users(created_at);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_status ON users(status);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_email_status ON users(email, status);

-- Tenants table indexes
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_tenants_tenant_id ON tenants(tenant_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_tenants_created_at ON tenants(created_at);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_tenants_status ON tenants(status);

-- Chatbots table indexes
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_chatbots_tenant_id ON chatbots(tenant_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_chatbots_created_at ON chatbots(created_at);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_chatbots_status ON chatbots(status);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_chatbots_tenant_created ON chatbots(tenant_id, created_at);

-- Messages table indexes
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_messages_chatbot_id ON messages(chatbot_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_messages_created_at ON messages(created_at);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_messages_sender ON messages(sender);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_messages_chatbot_created ON messages(chatbot_id, created_at);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_messages_tenant_id ON messages(tenant_id);

-- Packages table indexes
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_packages_active ON packages(is_active) WHERE is_active = true;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_packages_package_id ON packages(package_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_packages_created_at ON packages(created_at);

-- Subscriptions table indexes
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_subscriptions_tenant_id ON subscriptions(tenant_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_subscriptions_status ON subscriptions(status);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_subscriptions_created_at ON subscriptions(created_at);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_subscriptions_package_id ON subscriptions(package_id);

-- Payments table indexes
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_payments_tenant_id ON payments(tenant_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_payments_status ON payments(status);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_payments_created_at ON payments(created_at);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_payments_payment_method ON payments(payment_method);

-- Transactions table indexes
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_transactions_wallet_id ON transactions(wallet_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_transactions_created_at ON transactions(created_at);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_transactions_type ON transactions(type);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_transactions_status ON transactions(status);

-- Audit logs table indexes
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_audit_logs_tenant_id ON audit_logs(tenant_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_audit_logs_created_at ON audit_logs(created_at);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_audit_logs_action ON audit_logs(action);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_audit_logs_entity_type ON audit_logs(entity_type);

-- ========================================
-- 9. Create Partitioned Tables (for large datasets)
-- ========================================

-- Partition messages table by month
CREATE TABLE IF NOT EXISTS messages_partitioned (
    LIKE messages INCLUDING ALL
) PARTITION BY RANGE (created_at);

-- Create monthly partitions
DO $$
DECLARE
    start_date date;
    end_date date;
BEGIN
    -- Create partitions for current and next 11 months
    FOR i IN 0..11 LOOP
        start_date := date_trunc('month', CURRENT_DATE + interval '1 month' * i);
        end_date := start_date + interval '1 month';
        
        EXECUTE format('CREATE TABLE IF NOT EXISTS messages_%s PARTITION OF messages_partitioned
                       FOR VALUES FROM (%L) TO (%L)',
                       to_char(start_date, 'YYYY_MM'),
                       start_date,
                       end_date);
    END LOOP;
END $$;

-- ========================================
-- 10. Create Materialized Views
-- ========================================

-- User statistics materialized view
CREATE MATERIALIZED VIEW IF NOT EXISTS mv_user_stats AS
SELECT 
    t.tenant_id,
    COUNT(u.id) as total_users,
    COUNT(CASE WHEN u.created_at >= CURRENT_DATE - INTERVAL '30 days' THEN 1 END) as new_users_30d,
    COUNT(CASE WHEN u.last_login_at >= CURRENT_DATE - INTERVAL '7 days' THEN 1 END) as active_users_7d,
    COUNT(CASE WHEN u.last_login_at >= CURRENT_DATE - INTERVAL '1 day' THEN 1 END) as active_users_1d
FROM tenants t
LEFT JOIN users u ON t.tenant_id = u.tenant_id
GROUP BY t.tenant_id;

-- Chatbot statistics materialized view
CREATE MATERIALIZED VIEW IF NOT EXISTS mv_chatbot_stats AS
SELECT 
    t.tenant_id,
    COUNT(c.id) as total_chatbots,
    COUNT(CASE WHEN c.created_at >= CURRENT_DATE - INTERVAL '30 days' THEN 1 END) as new_chatbots_30d,
    COUNT(CASE WHEN c.status = 'ACTIVE' THEN 1 END) as active_chatbots
FROM tenants t
LEFT JOIN chatbots c ON t.tenant_id = c.tenant_id
GROUP BY t.tenant_id;

-- Message statistics materialized view
CREATE MATERIALIZED VIEW IF NOT EXISTS mv_message_stats AS
SELECT 
    t.tenant_id,
    c.id as chatbot_id,
    COUNT(m.id) as total_messages,
    COUNT(CASE WHEN m.created_at >= CURRENT_DATE - INTERVAL '1 day' THEN 1 END) as messages_1d,
    COUNT(CASE WHEN m.created_at >= CURRENT_DATE - INTERVAL '7 days' THEN 1 END) as messages_7d,
    COUNT(CASE WHEN m.created_at >= CURRENT_DATE - INTERVAL '30 days' THEN 1 END) as messages_30d
FROM tenants t
LEFT JOIN chatbots c ON t.tenant_id = c.tenant_id
LEFT JOIN messages m ON c.id = m.chatbot_id
GROUP BY t.tenant_id, c.id;

-- Create unique indexes for materialized views
CREATE UNIQUE INDEX IF NOT EXISTS idx_mv_user_stats_tenant_id ON mv_user_stats(tenant_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_mv_chatbot_stats_tenant_id ON mv_chatbot_stats(tenant_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_mv_message_stats_tenant_chatbot ON mv_message_stats(tenant_id, chatbot_id);

-- ========================================
-- 11. Create Refresh Functions
-- ========================================

-- Function to refresh materialized views
CREATE OR REPLACE FUNCTION refresh_materialized_views()
RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_user_stats;
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_chatbot_stats;
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_message_stats;
END;
$$ LANGUAGE plpgsql;

-- ========================================
-- 12. Create Performance Monitoring Functions
-- ========================================

-- Function to get slow queries
CREATE OR REPLACE FUNCTION get_slow_queries()
RETURNS TABLE(
    query text,
    calls bigint,
    total_time double precision,
    mean_time double precision,
    rows bigint
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        query,
        calls,
        total_exec_time as total_time,
        mean_exec_time as mean_time,
        rows
    FROM pg_stat_statements
    WHERE mean_exec_time > 1000  -- queries taking more than 1 second
    ORDER BY mean_exec_time DESC
    LIMIT 20;
END;
$$ LANGUAGE plpgsql;

-- Function to get table sizes
CREATE OR REPLACE FUNCTION get_table_sizes()
RETURNS TABLE(
    table_name text,
    size_mb numeric,
    index_size_mb numeric,
    total_size_mb numeric
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        schemaname||'.'||tablename as table_name,
        pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size_mb,
        pg_size_pretty(pg_indexes_size(schemaname||'.'||tablename)) as index_size_mb,
        pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as total_size_mb
    FROM pg_tables
    WHERE schemaname = 'public'
    ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
END;
$$ LANGUAGE plpgsql;

-- ========================================
-- 13. Create Auto-partitioning Function
-- ========================================

-- Function to create new monthly partitions
CREATE OR REPLACE FUNCTION create_monthly_partitions()
RETURNS void AS $$
DECLARE
    start_date date;
    end_date date;
    partition_name text;
BEGIN
    -- Create partition for next month
    start_date := date_trunc('month', CURRENT_DATE + interval '1 month');
    end_date := start_date + interval '1 month';
    partition_name := 'messages_' || to_char(start_date, 'YYYY_MM');
    
    EXECUTE format('CREATE TABLE IF NOT EXISTS %I PARTITION OF messages_partitioned
                   FOR VALUES FROM (%L) TO (%L)',
                   partition_name,
                   start_date,
                   end_date);
    
    -- Create index on new partition
    EXECUTE format('CREATE INDEX IF NOT EXISTS %I ON %I(chatbot_id, created_at)',
                   'idx_' || partition_name || '_chatbot_created',
                   partition_name);
END;
$$ LANGUAGE plpgsql;

-- ========================================
-- 14. Schedule Maintenance Tasks
-- ========================================

-- Create a job to refresh materialized views daily
-- (This would be scheduled with pg_cron or an external scheduler)

-- ========================================
-- 15. Performance Analysis
-- ========================================

-- Show current configuration
SELECT name, setting, unit, short_desc
FROM pg_settings
WHERE name IN (
    'shared_buffers', 'effective_cache_size', 'work_mem', 'maintenance_work_mem',
    'max_connections', 'checkpoint_completion_target', 'wal_buffers',
    'autovacuum_vacuum_threshold', 'autovacuum_vacuum_scale_factor',
    'log_min_duration_statement'
)
ORDER BY name;

-- Show index usage
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_tup_read,
    idx_tup_fetch,
    idx_scan
FROM pg_stat_user_indexes
ORDER BY idx_scan DESC;

-- Show table statistics
SELECT 
    schemaname,
    tablename,
    n_tup_ins,
    n_tup_upd,
    n_tup_del,
    n_live_tup,
    n_dead_tup,
    last_vacuum,
    last_autovacuum,
    last_analyze,
    last_autoanalyze
FROM pg_stat_user_tables
ORDER BY n_live_tup DESC;

COMMIT;
