-- Migration: Final verification and cleanup after FK fixes
-- This migration ensures data consistency and cleans up any remaining issues

-- =====================================================
-- STEP 1: Verify all foreign key constraints are properly created
-- =====================================================

-- Check and report current foreign key constraints
SELECT 
    tc.table_name,
    tc.constraint_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc 
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
    AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
    AND ccu.table_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY'
    AND tc.table_schema = current_schema()
    AND (tc.table_name IN ('user_info', 'tenant_members', 'tenant_join_requests', 'user_credentials', 'address'))
ORDER BY tc.table_name, tc.constraint_name;

-- =====================================================
-- STEP 2: Clean up any remaining data inconsistencies
-- =====================================================

-- Remove any orphaned user_info records (should not exist after proper migration)
DELETE FROM user_info 
WHERE user_id NOT IN (SELECT id FROM auth_users);

-- Remove any orphaned tenant_members records
DELETE FROM tenant_members 
WHERE user_id NOT IN (SELECT id FROM auth_users);

-- Remove any orphaned tenant_join_requests records  
DELETE FROM tenant_join_requests 
WHERE user_id NOT IN (SELECT id FROM auth_users);

-- Remove any orphaned user_credentials records
DELETE FROM user_credentials 
WHERE user_id NOT IN (SELECT id FROM users);

-- =====================================================
-- STEP 3: Ensure proper sequences and auto-increment
-- =====================================================

-- Reset sequences if needed (PostgreSQL specific)
DO $$
BEGIN
    -- Reset auth_users id sequence
    PERFORM setval(pg_get_serial_sequence('auth_users', 'id'), 
                   COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) 
    FROM auth_users;
    
    -- Reset users id sequence  
    PERFORM setval(pg_get_serial_sequence('users', 'id'), 
                   COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) 
    FROM users;
    
    -- Reset user_credentials user_id sequence if it has one
    IF EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_name = 'user_credentials_user_id_seq') THEN
        PERFORM setval(pg_get_serial_sequence('user_credentials', 'user_id'), 
                       COALESCE(MAX(user_id), 1), MAX(user_id) IS NOT NULL) 
        FROM user_credentials;
    END IF;
END $$;

-- =====================================================
-- STEP 4: Add missing unique constraints if needed
-- =====================================================

-- Ensure users.email is unique (should already exist from JPA)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'uk_users_email' 
        AND table_name = 'users'
    ) THEN
        ALTER TABLE users ADD CONSTRAINT uk_users_email UNIQUE (email);
    END IF;
END $$;

-- =====================================================
-- STEP 5: Final data integrity checks
-- =====================================================

-- Check for null emails in critical tables
SELECT 'users_with_null_email' as check_name, COUNT(*) as count
FROM users WHERE email IS NULL;

SELECT 'auth_users_with_null_email' as check_name, COUNT(*) as count  
FROM auth_users WHERE email IS NULL;

-- Check for inactive users that might need cleanup
SELECT 'inactive_auth_users' as check_name, COUNT(*) as count
FROM auth_users WHERE is_active = false OR status != 'ACTIVE';

SELECT 'inactive_identity_users' as check_name, COUNT(*) as count
FROM users WHERE status != 'ACTIVE';

-- Check credentials consistency
SELECT 'credentials_without_users' as check_name, COUNT(*) as count
FROM user_credentials uc 
LEFT JOIN users u ON uc.user_id = u.id 
WHERE u.id IS NULL;

SELECT 'users_without_credentials' as check_name, COUNT(*) as count
FROM users u 
LEFT JOIN user_credentials uc ON u.id = uc.user_id 
WHERE uc.user_id IS NULL;

-- =====================================================
-- STEP 6: Performance optimization
-- =====================================================

-- Analyze tables to update statistics
ANALYZE auth_users;
ANALYZE users;
ANALYZE user_credentials;
ANALYZE user_info;
ANALYZE tenant_members;
ANALYZE tenant_join_requests;
ANALYZE address;

-- =====================================================
-- STEP 7: Migration completion report
-- =====================================================

-- Generate final report
SELECT 'MIGRATION_REPORT' as report_type, 
       'V2__Fix_foreign_key_mismatches' as migration_name,
       'COMPLETED' as status,
       NOW() as completed_at;

-- Record counts for verification
SELECT 'FINAL_RECORD_COUNTS' as report_type,
    table_name,
    record_count
FROM (
    SELECT 'auth_users' as table_name, COUNT(*) as record_count FROM auth_users
    UNION ALL
    SELECT 'users' as table_name, COUNT(*) as record_count FROM users  
    UNION ALL
    SELECT 'user_credentials' as table_name, COUNT(*) as record_count FROM user_credentials
    UNION ALL
    SELECT 'user_info' as table_name, COUNT(*) as record_count FROM user_info
    UNION ALL
    SELECT 'tenant_members' as table_name, COUNT(*) as record_count FROM tenant_members
    UNION ALL
    SELECT 'tenant_join_requests' as table_name, COUNT(*) as record_count FROM tenant_join_requests
    UNION ALL
    SELECT 'address' as table_name, COUNT(*) as record_count FROM address
) final_counts
ORDER BY table_name;

-- Foreign key constraints summary
SELECT 'FOREIGN_KEY_CONSTRAINTS' as report_type,
    table_name,
    constraint_name,
    column_name,
    foreign_table_name,
    foreign_column_name
FROM (
    SELECT 
        tc.table_name,
        tc.constraint_name,
        kcu.column_name,
        ccu.table_name AS foreign_table_name,
        ccu.column_name AS foreign_column_name
    FROM information_schema.table_constraints AS tc 
    JOIN information_schema.key_column_usage AS kcu
        ON tc.constraint_name = kcu.constraint_name
        AND tc.table_schema = kcu.table_schema
    JOIN information_schema.constraint_column_usage AS ccu
        ON ccu.constraint_name = tc.constraint_name
        AND ccu.table_schema = tc.table_schema
    WHERE tc.constraint_type = 'FOREIGN KEY'
        AND tc.table_schema = current_schema()
        AND tc.table_name IN ('user_info', 'tenant_members', 'tenant_join_requests', 'user_credentials')
) fk_summary
ORDER BY table_name, constraint_name;
