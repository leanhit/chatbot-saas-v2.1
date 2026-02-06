-- Migration: Fix foreign key mismatches between JPA mappings and database schema
-- This aligns database schema with current JPA entity mappings after table rename

-- NOTE: This migration assumes V1__Fix_table_conflict.sql was already executed
-- and the legacy "users" table was renamed to "auth_users"

-- =====================================================
-- STEP 1: Update UserInfo table foreign key
-- =====================================================
-- UserInfo.java expects @JoinColumn(name = "user_id") referencing Auth
-- But after migration, the column should reference auth_users table

-- Drop existing foreign key constraint (if exists)
DO $$ 
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_user_info_auth_user' 
        AND table_name = 'user_info'
    ) THEN
        ALTER TABLE user_info DROP CONSTRAINT fk_user_info_auth_user;
    END IF;
END $$;

-- Ensure the column name matches JPA mapping (user_id, not auth_user_id)
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

-- Add proper foreign key constraint to auth_users table
ALTER TABLE user_info 
ADD CONSTRAINT fk_user_info_auth_user 
FOREIGN KEY (user_id) REFERENCES auth_users(id) 
ON DELETE CASCADE;

-- =====================================================
-- STEP 2: Update TenantMember table foreign key
-- =====================================================
-- TenantMember.java expects @JoinColumn(name = "user_id") referencing Auth

-- Drop existing foreign key constraint (if exists)
DO $$ 
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_tenant_members_auth_user' 
        AND table_name = 'tenant_members'
    ) THEN
        ALTER TABLE tenant_members DROP CONSTRAINT fk_tenant_members_auth_user;
    END IF;
END $$;

-- Ensure the column name matches JPA mapping (user_id, not auth_user_id)
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

-- Add proper foreign key constraint to auth_users table
ALTER TABLE tenant_members 
ADD CONSTRAINT fk_tenant_members_auth_user 
FOREIGN KEY (user_id) REFERENCES auth_users(id) 
ON DELETE CASCADE;

-- =====================================================
-- STEP 3: Update TenantJoinRequest table foreign key
-- =====================================================
-- TenantJoinRequest.java expects @JoinColumn(name = "user_id") referencing Auth

-- Drop existing foreign key constraint (if exists)
DO $$ 
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_tenant_join_requests_auth_user' 
        AND table_name = 'tenant_join_requests'
    ) THEN
        ALTER TABLE tenant_join_requests DROP CONSTRAINT fk_tenant_join_requests_auth_user;
    END IF;
END $$;

-- Ensure the column name matches JPA mapping (user_id, not auth_user_id)
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

-- Add proper foreign key constraint to auth_users table
ALTER TABLE tenant_join_requests 
ADD CONSTRAINT fk_tenant_join_requests_auth_user 
FOREIGN KEY (user_id) REFERENCES auth_users(id) 
ON DELETE CASCADE;

-- =====================================================
-- STEP 4: Update Address table owner_type values
-- =====================================================
-- Address table should use 'AUTH_USER' for legacy auth users
-- Keep 'USER' for Identity Hub users (new system)

-- Update existing addresses that reference auth_users IDs
UPDATE address 
SET owner_type = 'AUTH_USER' 
WHERE owner_type = 'USER' 
AND owner_id IN (
    SELECT id FROM auth_users
);

-- =====================================================
-- STEP 5: Create indexes for performance
-- =====================================================

-- auth_users table indexes
CREATE INDEX IF NOT EXISTS idx_auth_users_email ON auth_users(email);
CREATE INDEX IF NOT EXISTS idx_auth_users_status ON auth_users(status);
CREATE INDEX IF NOT EXISTS idx_auth_users_is_active ON auth_users(is_active);
CREATE INDEX IF NOT EXISTS idx_auth_users_system_role ON auth_users(system_role);

-- users table (Identity Hub) indexes
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);

-- user_credentials table indexes
CREATE INDEX IF NOT EXISTS idx_user_credentials_user_id ON user_credentials(user_id);
CREATE INDEX IF NOT EXISTS idx_user_credentials_source ON user_credentials(source);
CREATE INDEX IF NOT EXISTS idx_user_credentials_provider ON user_credentials(provider);
CREATE INDEX IF NOT EXISTS idx_user_credentials_is_active ON user_credentials(is_active);

-- user_info table indexes
CREATE INDEX IF NOT EXISTS idx_user_info_user_id ON user_info(user_id);
CREATE INDEX IF NOT EXISTS idx_user_info_phone_number ON user_info(phone_number);

-- tenant_members table indexes
CREATE INDEX IF NOT EXISTS idx_tenant_members_user_id ON tenant_members(user_id);
CREATE INDEX IF NOT EXISTS idx_tenant_members_tenant_id ON tenant_members(tenant_id);
CREATE INDEX IF NOT EXISTS idx_tenant_members_role ON tenant_members(role);

-- tenant_join_requests table indexes
CREATE INDEX IF NOT EXISTS idx_tenant_join_requests_user_id ON tenant_join_requests(user_id);
CREATE INDEX IF NOT EXISTS idx_tenant_join_requests_tenant_id ON tenant_join_requests(tenant_id);
CREATE INDEX IF NOT EXISTS idx_tenant_join_requests_status ON tenant_join_requests(status);

-- address table indexes
CREATE INDEX IF NOT EXISTS idx_address_owner_id ON address(owner_id);
CREATE INDEX IF NOT EXISTS idx_address_owner_type ON address(owner_type);
CREATE INDEX IF NOT EXISTS idx_address_tenant_id ON address(tenant_id);
CREATE INDEX IF NOT EXISTS idx_address_is_default ON address(is_default);

-- =====================================================
-- STEP 6: Verify data integrity
-- =====================================================

-- Check for orphaned records in user_info
SELECT 'orphaned_user_info' as check_name, COUNT(*) as count
FROM user_info ui 
LEFT JOIN auth_users a ON ui.user_id = a.id 
WHERE a.id IS NULL;

-- Check for orphaned records in tenant_members
SELECT 'orphaned_tenant_members' as check_name, COUNT(*) as count
FROM tenant_members tm 
LEFT JOIN auth_users a ON tm.user_id = a.id 
WHERE a.id IS NULL;

-- Check for orphaned records in tenant_join_requests
SELECT 'orphaned_tenant_join_requests' as check_name, COUNT(*) as count
FROM tenant_join_requests tjr 
LEFT JOIN auth_users a ON tjr.user_id = a.id 
WHERE a.id IS NULL;

-- Check for duplicate emails across auth_users and users tables
SELECT 'duplicate_emails' as check_name, COUNT(*) as count
FROM (
    SELECT email FROM auth_users 
    WHERE email IS NOT NULL
    INTERSECT
    SELECT email FROM users 
    WHERE email IS NOT NULL
) duplicates;

-- =====================================================
-- STEP 7: Add constraints for data consistency
-- =====================================================

-- Ensure user_info.user_id is not null
ALTER TABLE user_info 
ALTER COLUMN user_id SET NOT NULL;

-- Ensure tenant_members.user_id is not null
ALTER TABLE tenant_members 
ALTER COLUMN user_id SET NOT NULL;

-- Ensure tenant_join_requests.user_id is not null
ALTER TABLE tenant_join_requests 
ALTER COLUMN user_id SET NOT NULL;

-- =====================================================
-- STEP 8: Final verification
-- =====================================================

-- Summary of table structures after migration
SELECT 
    'auth_users' as table_name, 
    COUNT(*) as record_count
FROM auth_users
UNION ALL
SELECT 
    'users' as table_name, 
    COUNT(*) as record_count
FROM users
UNION ALL
SELECT 
    'user_credentials' as table_name, 
    COUNT(*) as record_count
FROM user_credentials
UNION ALL
SELECT 
    'user_info' as table_name, 
    COUNT(*) as record_count
FROM user_info
UNION ALL
SELECT 
    'tenant_members' as table_name, 
    COUNT(*) as record_count
FROM tenant_members
UNION ALL
SELECT 
    'tenant_join_requests' as table_name, 
    COUNT(*) as record_count
FROM tenant_join_requests;
