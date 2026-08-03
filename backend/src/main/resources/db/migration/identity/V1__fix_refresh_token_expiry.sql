-- ============================================================
-- V1: Fix refresh token expiry_date column
-- ============================================================

-- Ensure expiry_date column is properly defined
ALTER TABLE refresh_tokens 
ALTER COLUMN expiry_date SET NOT NULL;

-- Add default value for existing null records (if any)
UPDATE refresh_tokens 
SET expiry_date = created_date + INTERVAL '30 days'
WHERE expiry_date IS NULL;
