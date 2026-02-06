-- Check all foreign keys in the database
SELECT
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
ORDER BY tc.table_name, tc.constraint_name;

-- Check specific tables related to auth/identity
SELECT
    'TABLE_STRUCTURE' as info_type,
    table_name,
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_name IN ('users', 'auth_users', 'user_info', 'user_credentials', 'tenant_members', 'tenant_join_requests')
  AND table_schema = current_schema()
ORDER BY table_name, ordinal_position;
