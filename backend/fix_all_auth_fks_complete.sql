-- COMPLETE FIX FOR ALL AUTH-RELATED FOREIGN KEYS
-- This script fixes all FK constraints to reference auth_users table
-- Run this after individual table fixes or as a complete solution

-- ========================================
-- STEP 1: CHECK CURRENT STATE
-- ========================================

-- Check all auth-related foreign keys
SELECT
    'CURRENT_FK_STATE' as check_type,
    tc.table_name,
    tc.constraint_name,
    kcu.column_name,
    ccu.table_name AS referenced_table,
    ccu.column_name AS referenced_column
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu
  ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.key_column_usage ccu
  ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
  AND tc.table_schema = current_schema()
  AND tc.table_name IN ('user_info', 'tenant_members', 'tenant_join_requests')
ORDER BY tc.table_name, tc.constraint_name;

-- ========================================
-- STEP 2: FIX USER_INFO TABLE
-- ========================================

-- Drop existing FK constraints
ALTER TABLE user_info DROP CONSTRAINT IF EXISTS fkbvfqse8k5w3yc5igairr1g2hc;
ALTER TABLE user_info DROP CONSTRAINT IF EXISTS fk_user_info_auth_user;

-- Ensure correct column name
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'user_info' 
        AND column_name = 'auth_user_id'
    ) THEN
        ALTER TABLE user_info RENAME COLUMN auth_user_id TO user_id;
    END IF;
END $$;

-- Add proper FK constraint
ALTER TABLE user_info 
ADD CONSTRAINT fk_user_info_auth_user 
FOREIGN KEY (user_id) REFERENCES auth_users(id) ON DELETE CASCADE;

-- ========================================
-- STEP 3: FIX TENANT_MEMBERS TABLE
-- ========================================

-- Drop existing FK constraints
ALTER TABLE tenant_members DROP CONSTRAINT IF EXISTS fktm3h8k5w3yc5igairr1g2hc;
ALTER TABLE tenant_members DROP CONSTRAINT IF EXISTS fk_tenant_members_auth_user;

-- Ensure correct column name
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'tenant_members' 
        AND column_name = 'auth_user_id'
    ) THEN
        ALTER TABLE tenant_members RENAME COLUMN auth_user_id TO user_id;
    END IF;
END $$;

-- Add proper FK constraint
ALTER TABLE tenant_members 
ADD CONSTRAINT fk_tenant_members_auth_user 
FOREIGN KEY (user_id) REFERENCES auth_users(id) ON DELETE CASCADE;

-- ========================================
-- STEP 4: FIX TENANT_JOIN_REQUESTS TABLE
-- ========================================

-- Drop existing FK constraints
ALTER TABLE tenant_join_requests DROP CONSTRAINT IF EXISTS fktjr4h8k5w3yc5igairr1g2hc;
ALTER TABLE tenant_join_requests DROP CONSTRAINT IF EXISTS fk_tenant_join_requests_auth_user;

-- Ensure correct column name
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'tenant_join_requests' 
        AND column_name = 'auth_user_id'
    ) THEN
        ALTER TABLE tenant_join_requests RENAME COLUMN auth_user_id TO user_id;
    END IF;
END $$;

-- Add proper FK constraint
ALTER TABLE tenant_join_requests 
ADD CONSTRAINT fk_tenant_join_requests_auth_user 
FOREIGN KEY (user_id) REFERENCES auth_users(id) ON DELETE CASCADE;

-- ========================================
-- STEP 5: VERIFY ALL FIXES
-- ========================================

SELECT
    'FIXED_FK_STATE' as check_type,
    tc.table_name,
    tc.constraint_name,
    kcu.column_name,
    ccu.table_name AS referenced_table,
    ccu.column_name AS referenced_column
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu
  ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.key_column_usage ccu
  ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
  AND tc.table_schema = current_schema()
  AND tc.table_name IN ('user_info', 'tenant_members', 'tenant_join_requests')
ORDER BY tc.table_name, tc.constraint_name;

-- ========================================
-- STEP 6: CHECK FOR ORPHANED RECORDS
-- ========================================

SELECT 'ORPHANED_USER_INFO' as check_type, COUNT(*) as orphaned_count
FROM user_info ui
LEFT JOIN auth_users a ON ui.user_id = a.id
WHERE a.id IS NULL

UNION ALL

SELECT 'ORPHANED_TENANT_MEMBERS' as check_type, COUNT(*) as orphaned_count
FROM tenant_members tm
LEFT JOIN auth_users a ON tm.user_id = a.id
WHERE a.id IS NULL

UNION ALL

SELECT 'ORPHANED_TENANT_JOIN_REQUESTS' as check_type, COUNT(*) as orphaned_count
FROM tenant_join_requests tjr
LEFT JOIN auth_users a ON tjr.user_id = a.id
WHERE a.id IS NULL;

-- ========================================
-- STEP 7: CLEAN UP ORPHANED RECORDS (OPTIONAL)
-- ========================================

-- Uncomment these lines if you want to automatically clean up orphaned records
-- DELETE FROM user_info WHERE user_id NOT IN (SELECT id FROM auth_users);
-- DELETE FROM tenant_members WHERE user_id NOT IN (SELECT id FROM auth_users);
-- DELETE FROM tenant_join_requests WHERE user_id NOT IN (SELECT id FROM auth_users);

-- ========================================
-- STEP 8: FINAL VERIFICATION
-- ========================================

-- Expected result should show:
-- table_name           | constraint_name                 | column_name | referenced_table | referenced_column
-- --------------------|----------------------------------|-------------|------------------|------------------
-- user_info           | fk_user_info_auth_user           | user_id     | auth_users       | id
-- tenant_members      | fk_tenant_members_auth_user      | user_id     | auth_users       | id
-- tenant_join_requests| fk_tenant_join_requests_auth_user | user_id     | auth_users       | id

SELECT 'MIGRATION_COMPLETE' as status, NOW() as completed_at;
