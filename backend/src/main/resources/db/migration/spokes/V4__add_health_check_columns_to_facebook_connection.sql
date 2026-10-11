-- ============================================================
-- V4: Add health check columns to facebook_connection table
-- ============================================================

ALTER TABLE facebook_connection 
ADD COLUMN IF NOT EXISTS is_healthy BOOLEAN DEFAULT TRUE;

ALTER TABLE facebook_connection 
ADD COLUMN IF NOT EXISTS last_used_at TIMESTAMP;

ALTER TABLE facebook_connection 
ADD COLUMN IF NOT EXISTS last_health_check_at TIMESTAMP;

ALTER TABLE facebook_connection 
ADD COLUMN IF NOT EXISTS health_check_failures INTEGER DEFAULT 0;
