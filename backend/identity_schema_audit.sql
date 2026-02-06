-- IDENTITY SCHEMA AUDIT - READ ONLY
-- Purpose: Verify current database schema for Identity system
-- Database: traloitudong_db
-- User: traloitudong_user

-- =====================================================
-- 1. BASIC CONNECTION INFO
-- =====================================================

SELECT 'CONNECTION_INFO' as audit_type,
       current_database() as database_name,
       current_user() as current_user,
       session_user() as session_user,
       version() as postgresql_version;

-- Show current search path
SELECT 'SEARCH_PATH' as audit_type,
       current_setting('search_path') as search_path;

-- =====================================================
-- 2. LIST ALL TABLES IN PUBLIC SCHEMA
-- =====================================================

SELECT 'ALL_PUBLIC_TABLES' as audit_type,
       table_name,
       table_type,
       tableowner as owner
FROM pg_catalog.pg_tables 
WHERE schemaname = 'public'
ORDER BY table_name;

-- =====================================================
-- 3. CHECK IDENTITY-RELATED TABLES EXISTENCE
-- =====================================================

SELECT 'IDENTITY_TABLES_EXISTENCE' as audit_type,
       table_name,
       CASE 
           WHEN EXISTS (
               SELECT 1 FROM information_schema.tables 
               WHERE table_schema = 'public' 
               AND table_name = 'users'
           ) THEN 'EXISTS'
           ELSE 'NOT EXISTS'
       END as status
WHERE table_name = 'users'

UNION ALL

SELECT 'IDENTITY_TABLES_EXISTENCE' as audit_type,
       table_name,
       CASE 
           WHEN EXISTS (
               SELECT 1 FROM information_schema.tables 
               WHERE table_schema = 'public' 
               AND table_name = 'user_credentials'
           ) THEN 'EXISTS'
           ELSE 'NOT_EXISTS'
       END as status
WHERE table_name = 'user_credentials'

UNION ALL

SELECT 'IDENTITY_TABLES_EXISTENCE' as audit_type,
       table_name,
       CASE 
           WHEN EXISTS (
               SELECT 1 FROM information_schema.tables 
               WHERE table_schema = 'public' 
               AND table_name = 'identity_users'
           ) THEN 'EXISTS'
           ELSE 'NOT_EXISTS'
       END as status
WHERE table_name = 'identity_users'

UNION ALL

SELECT 'IDENTITY_TABLES_EXISTENCE' as audit_type,
       table_name,
       CASE 
           WHEN EXISTS (
               SELECT 1 FROM information_schema.tables 
               WHERE table_schema = 'public' 
               AND table_name = 'user_tenants'
           ) THEN 'EXISTS'
           ELSE 'NOT_EXISTS'
       END as status
WHERE table_name = 'user_tenants';

-- =====================================================
-- 4. TABLE OWNERSHIP
-- =====================================================

SELECT 'TABLE_OWNERSHIP' as audit_type,
       table_name,
       tableowner as owner
FROM pg_catalog.pg_tables 
WHERE schemaname = 'public'
  AND table_name IN ('users', 'user_credentials', 'identity_users', 'user_tenants')
ORDER BY table_name;

-- =====================================================
-- 5. USER PRIVILEGES ON IDENTITY TABLES
-- =====================================================

SELECT 'USER_PRIVILEGES' as audit_type,
       table_name,
       privilege_type,
       grantee
FROM information_schema.role_table_grants 
WHERE table_schema = 'public'
  AND table_name IN ('users', 'user_credentials', 'identity_users', 'user_tenants')
  AND grantee = current_user
ORDER BY table_name, privilege_type;

-- =====================================================
-- 6. DETAILED TABLE STRUCTURES
-- =====================================================

SELECT 'USERS_TABLE_STRUCTURE' as audit_type,
       column_name,
       data_type,
       is_nullable,
       column_default,
       character_maximum_length
FROM information_schema.columns 
WHERE table_name = 'users' 
  AND table_schema = 'public'
ORDER BY ordinal_position;

SELECT 'USER_CREDENTIALS_TABLE_STRUCTURE' as audit_type,
       column_name,
       data_type,
       is_nullable,
       column_default,
       character_maximum_length
FROM information_schema.columns 
WHERE table_name = 'user_credentials' 
  AND table_schema = 'public'
ORDER BY ordinal_position;

-- =====================================================
-- 7. CONSTRAINTS AND INDEXES
-- =====================================================

SELECT 'USERS_TABLE_CONSTRAINTS' as audit_type,
       constraint_name,
       constraint_type,
       column_name
FROM information_schema.table_constraints tc
LEFT JOIN information_schema.key_column_usage kcu 
  ON tc.constraint_name = kcu.constraint_name
WHERE tc.table_name = 'users' 
  AND tc.table_schema = 'public'
ORDER BY constraint_type, constraint_name;

SELECT 'USER_CREDENTIALS_TABLE_CONSTRAINTS' as audit_type,
       constraint_name,
       constraint_type,
       column_name
FROM information_schema.table_constraints tc
LEFT JOIN information_schema.key_column_usage kcu 
  ON tc.constraint_name = kcu.constraint_name
WHERE tc.table_name = 'user_credentials' 
  AND tc.table_schema = 'public'
ORDER BY constraint_type, constraint_name;

-- =====================================================
-- 8. RECORD COUNTS (READ ONLY)
-- =====================================================

SELECT 'RECORD_COUNTS' as audit_type,
    table_name,
    record_count
FROM (
    SELECT 'users' as table_name, COUNT(*) as record_count FROM users
    UNION ALL
    SELECT 'user_credentials' as table_name, COUNT(*) as record_count FROM user_credentials
    UNION ALL
    SELECT 'identity_users' as table_name, COUNT(*) as record_count FROM identity_users
    UNION ALL
    SELECT 'user_tenants' as table_name, COUNT(*) as record_count FROM user_tenants
) counts
WHERE EXISTS (
    SELECT 1 FROM information_schema.tables 
    WHERE table_schema = 'public' 
    AND table_name = counts.table_name
)
ORDER BY table_name;

-- =====================================================
-- 9. SAMPLE DATA VERIFICATION (READ ONLY)
-- =====================================================

SELECT 'USERS_SAMPLE_DATA' as audit_type,
       id,
       email,
       status,
       created_at
FROM users
LIMIT 3;

SELECT 'USER_CREDENTIALS_SAMPLE_DATA' as audit_type,
       user_id,
       provider,
       source,
       is_active,
       created_at
FROM user_credentials
LIMIT 3;
