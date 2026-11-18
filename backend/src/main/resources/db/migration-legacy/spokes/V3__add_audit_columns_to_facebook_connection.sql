-- ============================================================
-- V3: Add missing audit columns to facebook_connection table
-- ============================================================

ALTER TABLE facebook_connection 
ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
