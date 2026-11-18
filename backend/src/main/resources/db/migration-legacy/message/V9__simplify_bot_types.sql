-- ============================================================
-- V9: Simplify bot types from 8 to 3 core types
-- ============================================================
-- Migration mapping:
-- CUSTOMER_SERVICE, SUPPORT → SUPPORT
-- SALES, MARKETING → BUSINESS
-- HR, FINANCE → GENERAL
-- GENERAL, BOTPRESS → unchanged
-- ============================================================

-- Update CUSTOMER_SERVICE to SUPPORT
UPDATE penny_bots
SET bot_type = 'SUPPORT'
WHERE bot_type = 'CUSTOMER_SERVICE';

-- Update SUPPORT (keep as is, but ensure consistency)
-- No action needed

-- Update SALES to BUSINESS
UPDATE penny_bots
SET bot_type = 'BUSINESS'
WHERE bot_type = 'SALES';

-- Update MARKETING to BUSINESS
UPDATE penny_bots
SET bot_type = 'BUSINESS'
WHERE bot_type = 'MARKETING';

-- Update HR to GENERAL
UPDATE penny_bots
SET bot_type = 'GENERAL'
WHERE bot_type = 'HR';

-- Update FINANCE to GENERAL
UPDATE penny_bots
SET bot_type = 'GENERAL'
WHERE bot_type = 'FINANCE';

-- GENERAL and BOTPRESS remain unchanged
-- No action needed

-- Add CHECK constraint to ensure only valid bot types
ALTER TABLE penny_bots
DROP CONSTRAINT IF EXISTS chk_bot_type;

ALTER TABLE penny_bots
ADD CONSTRAINT chk_bot_type
CHECK (bot_type IN ('GENERAL', 'SUPPORT', 'BUSINESS', 'BOTPRESS'));

-- Log migration
DO $$
BEGIN
    RAISE NOTICE 'Bot types simplified from 8 to 3 core types';
END $$;
