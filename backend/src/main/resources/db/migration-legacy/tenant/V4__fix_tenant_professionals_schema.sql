-- ============================================================
-- V4: Fix tenant_professionals schema for existing databases
-- ============================================================

ALTER TABLE tenant_professionals ADD COLUMN IF NOT EXISTS job_title VARCHAR(255);
ALTER TABLE tenant_professionals ADD COLUMN IF NOT EXISTS department VARCHAR(255);
ALTER TABLE tenant_professionals ADD COLUMN IF NOT EXISTS company VARCHAR(200);
ALTER TABLE tenant_professionals ADD COLUMN IF NOT EXISTS linkedin_url VARCHAR(255);
ALTER TABLE tenant_professionals ADD COLUMN IF NOT EXISTS website VARCHAR(255);
ALTER TABLE tenant_professionals ADD COLUMN IF NOT EXISTS location VARCHAR(255);
ALTER TABLE tenant_professionals ADD COLUMN IF NOT EXISTS skills TEXT;
ALTER TABLE tenant_professionals ADD COLUMN IF NOT EXISTS experience TEXT;
ALTER TABLE tenant_professionals ADD COLUMN IF NOT EXISTS education TEXT;
ALTER TABLE tenant_professionals ADD COLUMN IF NOT EXISTS certifications TEXT;
ALTER TABLE tenant_professionals ADD COLUMN IF NOT EXISTS languages VARCHAR(255);
ALTER TABLE tenant_professionals ADD COLUMN IF NOT EXISTS availability VARCHAR(255);
ALTER TABLE tenant_professionals ADD COLUMN IF NOT EXISTS hourly_rate VARCHAR(100);
ALTER TABLE tenant_professionals ADD COLUMN IF NOT EXISTS portfolio_url VARCHAR(255);
