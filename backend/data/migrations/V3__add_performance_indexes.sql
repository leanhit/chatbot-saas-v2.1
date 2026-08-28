-- Performance Optimization: Add Missing Database Indexes
-- This migration adds indexes for frequently used queries to improve performance
-- Analyzed from repository query patterns and entity relationships

-- ============================================================================
-- MESSAGES TABLE INDEXES
-- ============================================================================

-- Index for finding messages by conversation and tenant (most frequent query)
CREATE INDEX IF NOT EXISTS idx_message_conversation_tenant 
ON messages(conversation_id, tenant_id);

-- Index for finding messages by conversation, tenant, and created_at (for sorting)
CREATE INDEX IF NOT EXISTS idx_message_conversation_tenant_created 
ON messages(conversation_id, tenant_id, created_at DESC);

-- Index for finding messages by conversation, tenant, and read status
CREATE INDEX IF NOT EXISTS idx_message_conversation_tenant_read 
ON messages(conversation_id, tenant_id, is_read);

-- Index for finding messages by external message id and tenant (for idempotency)
CREATE INDEX IF NOT EXISTS idx_message_external_tenant 
ON messages(external_message_id, tenant_id);

-- Index for finding messages by sender and tenant (for statistics)
CREATE INDEX IF NOT EXISTS idx_message_sender_tenant 
ON messages(sender, tenant_id);

-- Index for finding messages by ID and tenant (for security)
CREATE INDEX IF NOT EXISTS idx_message_id_tenant 
ON messages(id, tenant_id);

-- ============================================================================
-- FACEBOOK_CONNECTION TABLE INDEXES
-- ============================================================================

-- Index for finding connections by pageId (webhook lookup - very frequent)
CREATE INDEX IF NOT EXISTS idx_fb_connection_page 
ON facebook_connection(page_id);

-- Index for finding connections by pageId, enabled, and active (webhook lookup)
CREATE INDEX IF NOT EXISTS idx_fb_connection_page_enabled_active 
ON facebook_connection(page_id, is_enabled, is_active);

-- Index for finding connections by tenant and page (unique constraint already exists, but index helps queries)
CREATE INDEX IF NOT EXISTS idx_fb_connection_tenant_page 
ON facebook_connection(tenant_id, page_id);

-- Index for finding connections by tenant, page, and active
CREATE INDEX IF NOT EXISTS idx_fb_connection_tenant_page_active 
ON facebook_connection(tenant_id, page_id, is_active);

-- Index for finding connections by owner and tenant
CREATE INDEX IF NOT EXISTS idx_fb_connection_owner_tenant 
ON facebook_connection(owner_id, tenant_id);

-- Index for finding connections by owner, tenant, and active
CREATE INDEX IF NOT EXISTS idx_fb_connection_owner_tenant_active 
ON facebook_connection(owner_id, tenant_id, is_active);

-- Index for finding connections by botId and tenant
CREATE INDEX IF NOT EXISTS idx_fb_connection_bot_tenant 
ON facebook_connection(bot_id, tenant_id);

-- Index for finding connections by botId, tenant, and active (for agent messages)
CREATE INDEX IF NOT EXISTS idx_fb_connection_bot_tenant_active 
ON facebook_connection(bot_id, tenant_id, is_active);

-- Index for finding connections by botId and active (without tenant - for agent messages)
CREATE INDEX IF NOT EXISTS idx_fb_connection_bot_active 
ON facebook_connection(bot_id, is_active);

-- Index for finding connections by tenant and active
CREATE INDEX IF NOT EXISTS idx_fb_connection_tenant_active 
ON facebook_connection(tenant_id, is_active);

-- Index for finding connections by token expiry
CREATE INDEX IF NOT EXISTS idx_fb_connection_token_expiry 
ON facebook_connection(token_expires_at, is_active);

-- ============================================================================
-- PENNY_BOTS TABLE INDEXES
-- ============================================================================

-- Index for finding bots by tenant and active status (most frequent)
CREATE INDEX IF NOT EXISTS idx_penny_bot_tenant_active 
ON penny_bots(tenant_id, is_active);

-- Index for finding bots by tenant, active, and created_at (for sorting)
CREATE INDEX IF NOT EXISTS idx_penny_bot_tenant_active_created 
ON penny_bots(tenant_id, is_active, created_at DESC);

-- Index for finding bots by owner and active
CREATE INDEX IF NOT EXISTS idx_penny_bot_owner_active 
ON penny_bots(owner_id, is_active);

-- Index for finding bots by tenant, bot type, and active
CREATE INDEX IF NOT EXISTS idx_penny_bot_tenant_type_active 
ON penny_bots(tenant_id, bot_type, is_active);

-- Index for finding bots by tenant and owner
CREATE INDEX IF NOT EXISTS idx_penny_bot_tenant_owner 
ON penny_bots(tenant_id, owner_id);

-- Index for finding bots by tenant, owner, and active
CREATE INDEX IF NOT EXISTS idx_penny_bot_tenant_owner_active 
ON penny_bots(tenant_id, owner_id, is_active);

-- Index for finding bots by pennyBotId and active
CREATE INDEX IF NOT EXISTS idx_penny_bot_penny_id_active 
ON penny_bots(penny_bot_id, is_active);

-- Index for searching bots by tenant, active, and bot name (LIKE query)
CREATE INDEX IF NOT EXISTS idx_penny_bot_tenant_active_name 
ON penny_bots(tenant_id, is_active, bot_name);

-- ============================================================================
-- TENANT_MEMBERS TABLE INDEXES
-- ============================================================================

-- Index for finding members by tenant and user (most frequent)
CREATE INDEX IF NOT EXISTS idx_tenant_member_tenant_user 
ON tenant_members(tenant_id, user_id);

-- Index for finding members by tenant, user, and status
CREATE INDEX IF NOT EXISTS idx_tenant_member_tenant_user_status 
ON tenant_members(tenant_id, user_id, status);

-- Index for finding members by tenant and status
CREATE INDEX IF NOT EXISTS idx_tenant_member_tenant_status 
ON tenant_members(tenant_id, status);

-- Index for finding members by user and status
CREATE INDEX IF NOT EXISTS idx_tenant_member_user_status 
ON tenant_members(user_id, status);

-- Index for finding members by user (for finding user's tenants)
CREATE INDEX IF NOT EXISTS idx_tenant_member_user 
ON tenant_members(user_id);

-- Index for finding members by tenant and role
CREATE INDEX IF NOT EXISTS idx_tenant_member_tenant_role 
ON tenant_members(tenant_id, role);

-- Index for finding members by tenant, user, and role
CREATE INDEX IF NOT EXISTS idx_tenant_member_tenant_user_role 
ON tenant_members(tenant_id, user_id, role);

-- ============================================================================
-- CONVERSATIONS TABLE - ADDITIONAL INDEXES
-- ============================================================================

-- Index for finding conversations by tenant and updated_at (for sorting)
CREATE INDEX IF NOT EXISTS idx_conversation_tenant_updated 
ON conversations(tenant_id, updated_at DESC);

-- Index for finding conversations by tenant, status, and agent
CREATE INDEX IF NOT EXISTS idx_conversation_tenant_status_agent 
ON conversations(tenant_id, status, agent_assigned_id);

-- Index for finding conversations by tenant, owner, and updated_at
CREATE INDEX IF NOT EXISTS idx_conversation_tenant_owner_updated 
ON conversations(tenant_id, owner_id, updated_at DESC);

-- Index for finding conversations by tenant and takeover status
CREATE INDEX IF NOT EXISTS idx_conversation_tenant_takeover 
ON conversations(tenant_id, is_taken_over_by_agent);

-- Index for finding conversations by tenant, owner, connection, and updated_at
CREATE INDEX IF NOT EXISTS idx_conversation_tenant_owner_conn_updated 
ON conversations(tenant_id, owner_id, connection_id, updated_at DESC);

-- Index for finding conversations by tenant and id (for security)
CREATE INDEX IF NOT EXISTS idx_conversation_tenant_id 
ON conversations(tenant_id, id);

-- Index for finding conversations by connection and external user (without tenant)
CREATE INDEX IF NOT EXISTS idx_conversation_conn_external 
ON conversations(connection_id, external_user_id);

-- Index for searching conversations by tenant, owner, and query (LIKE on user_name/external_user_id)
-- Note: PostgreSQL can use btree index for LIKE with pattern at end, but not for leading wildcard
-- For full-text search, consider gin index with to_tsvector in future
CREATE INDEX IF NOT EXISTS idx_conversation_tenant_owner_name 
ON conversations(tenant_id, owner_id, user_name);

-- Index for finding conversations by tenant and created_at (for date range queries)
CREATE INDEX IF NOT EXISTS idx_conversation_tenant_created 
ON conversations(tenant_id, created_at);

-- Index for finding conversations by tenant, status, and resolution_status
CREATE INDEX IF NOT EXISTS idx_conversation_tenant_status_resolution 
ON conversations(tenant_id, status, resolution_status);

-- ============================================================================
-- AGENTS TABLE - ADDITIONAL INDEXES
-- ============================================================================

-- Index for finding agents by tenant and user
CREATE INDEX IF NOT EXISTS idx_agent_tenant_user 
ON agents(tenant_id, user_id);

-- Index for finding agents by tenant and status
CREATE INDEX IF NOT EXISTS idx_agent_tenant_status 
ON agents(tenant_id, status);

-- Index for finding agents by tenant and active
CREATE INDEX IF NOT EXISTS idx_agent_tenant_active 
ON agents(tenant_id, active);

-- Index for finding agents by tenant, status, and active
CREATE INDEX IF NOT EXISTS idx_agent_tenant_status_active 
ON agents(tenant_id, status, active);

-- ============================================================================
-- COMMENTS FOR DOCUMENTATION
-- ============================================================================

COMMENT ON INDEX idx_message_conversation_tenant IS 'Optimize message lookup by conversation and tenant';
COMMENT ON INDEX idx_fb_connection_page IS 'Optimize webhook lookup by pageId';
COMMENT ON INDEX idx_penny_bot_tenant_active IS 'Optimize bot lookup by tenant and active status';
COMMENT ON INDEX idx_tenant_member_tenant_user IS 'Optimize member lookup by tenant and user';
COMMENT ON INDEX idx_conversation_tenant_updated IS 'Optimize conversation sorting by updated_at';

-- ============================================================================
-- PERFORMANCE NOTES
-- ============================================================================
-- 
-- 1. Composite indexes are ordered by selectivity (most selective first)
-- 2. DESC indexes are used for ORDER BY DESC queries
-- 3. Partial indexes (with WHERE clause) could be added for specific use cases
-- 4. Consider using EXPLAIN ANALYZE to verify index usage after deployment
-- 5. Monitor index bloat and consider REINDEX if performance degrades
-- 6. For very large tables, consider CONCURRENTLY option for index creation
--
-- ============================================================================
