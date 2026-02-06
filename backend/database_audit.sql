-- ========================================
-- DATABASE AUDIT & SYNCHRONIZATION CHECK
-- ========================================

-- 1. CONNECTION VERIFICATION
SELECT 'CONNECTION_INFO' as check_type, 
       current_database() as database_name,
       current_user() as current_user,
       version() as postgresql_version;

-- 2. SCHEMA STATUS - Check all tables
SELECT 'SCHEMA_TABLES' as check_type,
       table_name,
       table_type
FROM information_schema.tables 
WHERE table_schema = current_schema()
  AND table_name IN ('users', 'auth_users', 'user_credentials', 'user_info', 'tenant_members', 'tenant_join_requests', 'address')
ORDER BY table_name;

-- 3. TABLE STRUCTURE COMPARISON
-- Check if identity tables exist and match Java entities
SELECT 'USERS_TABLE_STRUCTURE' as check_type,
       column_name,
       data_type,
       is_nullable,
       column_default,
       character_maximum_length
FROM information_schema.columns 
WHERE table_name = 'users' 
  AND table_schema = current_schema()
ORDER BY ordinal_position;

SELECT 'AUTH_USERS_TABLE_STRUCTURE' as check_type,
       column_name,
       data_type,
       is_nullable,
       column_default,
       character_maximum_length
FROM information_schema.columns 
WHERE table_name = 'auth_users' 
  AND table_schema = current_schema()
ORDER BY ordinal_position;

SELECT 'USER_CREDENTIALS_TABLE_STRUCTURE' as check_type,
       column_name,
       data_type,
       is_nullable,
       column_default,
       character_maximum_length
FROM information_schema.columns 
WHERE table_name = 'user_credentials' 
  AND table_schema = current_schema()
ORDER BY ordinal_position;

-- 4. ENUM TYPE CHECK - Verify status column types
SELECT 'STATUS_COLUMN_TYPES' as check_type,
       table_name,
       column_name,
       data_type,
       udt_name,
       is_nullable
FROM information_schema.columns 
WHERE column_name = 'status' 
  AND table_schema = current_schema()
  AND table_name IN ('users', 'auth_users');

-- 5. FOREIGN KEY CONSTRAINTS CHECK
SELECT 'FOREIGN_KEYS' as check_type,
       tc.table_name,
       tc.constraint_name,
       kcu.column_name,
       ccu.table_name AS referenced_table,
       ccu.column_name AS referenced_column
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu
  ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage ccu
  ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
  AND tc.table_schema = current_schema()
  AND tc.table_name IN ('user_info', 'user_credentials', 'tenant_members', 'tenant_join_requests')
ORDER BY tc.table_name, tc.constraint_name;

-- 6. UNIQUE CONSTRAINTS CHECK
SELECT 'UNIQUE_CONSTRAINTS' as check_type,
       tc.table_name,
       tc.constraint_name,
       kcu.column_name
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu
  ON tc.constraint_name = kcu.constraint_name
WHERE tc.constraint_type = 'UNIQUE'
  AND tc.table_schema = current_schema()
  AND tc.table_name IN ('users', 'auth_users', 'user_credentials')
ORDER BY tc.table_name, tc.constraint_name;

-- 7. MIGRATION STATUS CHECK
-- Check if migration tracking tables exist
SELECT 'MIGRATION_TABLES' as check_type,
       table_name,
       table_type
FROM information_schema.tables 
WHERE table_schema = current_schema()
  AND (table_name LIKE '%schema_version%' 
       OR table_name LIKE '%flyway%' 
       OR table_name LIKE '%liquibase%');

-- 8. DATA AUDIT - Check for potential conflicts
SELECT 'EMAIL_DUPLICATES' as check_type,
       email,
       COUNT(*) as duplicate_count
FROM (
    SELECT email FROM users WHERE email IS NOT NULL
    UNION ALL
    SELECT email FROM auth_users WHERE email IS NOT NULL
) all_emails
GROUP BY email
HAVING COUNT(*) > 1;

-- 9. ORPHANED RECORDS CHECK
SELECT 'ORPHANED_USER_INFO' as check_type,
       COUNT(*) as orphaned_count
FROM user_info ui
LEFT JOIN auth_users a ON ui.user_id = a.id
WHERE a.id IS NULL;

SELECT 'ORPHANED_USER_CREDENTIALS' as check_type,
       COUNT(*) as orphaned_count
FROM user_credentials uc
LEFT JOIN users u ON uc.user_id = u.id
WHERE u.id IS NULL;

-- 10. DATA COUNT SUMMARY
SELECT 'RECORD_COUNTS' as check_type,
    table_name,
    record_count
FROM (
    SELECT 'users' as table_name, COUNT(*) as record_count FROM users
    UNION ALL
    SELECT 'auth_users' as table_name, COUNT(*) as record_count FROM auth_users
    UNION ALL
    SELECT 'user_credentials' as table_name, COUNT(*) as record_count FROM user_credentials
    UNION ALL
    SELECT 'user_info' as table_name, COUNT(*) as record_count FROM user_info
    UNION ALL
    SELECT 'tenant_members' as table_name, COUNT(*) as record_count FROM tenant_members
    UNION ALL
    SELECT 'tenant_join_requests' as table_name, COUNT(*) as record_count FROM tenant_join_requests
) counts
ORDER BY table_name;

-- 11. INDEX CHECK
SELECT 'INDEXES' as check_type,
       schemaname,
       tablename,
       indexname,
       indexdef
FROM pg_indexes
WHERE schemaname = current_schema()
  AND tablename IN ('users', 'auth_users', 'user_credentials', 'user_info')
ORDER BY tablename, indexname;

-- 12. SEQUENCE CHECK
SELECT 'SEQUENCES' as check_type,
       sequence_name,
       start_value,
       increment_by,
       max_value,
       last_value
FROM information_schema.sequences
WHERE sequence_schema = current_schema()
ORDER BY sequence_name;
