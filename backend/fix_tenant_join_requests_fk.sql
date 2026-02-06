-- Fix tenant_join_requests foreign key to reference auth_users table
-- Run this to fix the FK constraint for tenant_join_requests table

-- Step 1: Check current FK constraint
SELECT
    tc.constraint_name,
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS referenced_table,
    ccu.column_name AS referenced_column
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu
  ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.key_column_usage ccu
  ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
  AND tc.table_name = 'tenant_join_requests';

-- Step 2: Check column structure
SELECT 
    column_name,
    data_type,
    is_nullable
FROM information_schema.columns 
WHERE table_name = 'tenant_join_requests' 
  AND table_schema = current_schema()
  AND column_name LIKE '%user%'
ORDER BY ordinal_position;

-- Step 3: Drop existing FK constraint if it exists
ALTER TABLE tenant_join_requests DROP CONSTRAINT IF EXISTS fk_tenant_join_requests_auth_user;
ALTER TABLE tenant_join_requests DROP CONSTRAINT IF EXISTS fktjr4h8k5w3yc5igairr1g2hc; -- Replace with actual constraint name if different

-- Step 4: Ensure column name matches JPA mapping (user_id)
-- If column was renamed to auth_user_id, rename it back to user_id
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

-- Step 5: Add proper foreign key constraint to auth_users table
ALTER TABLE tenant_join_requests 
ADD CONSTRAINT fk_tenant_join_requests_auth_user 
FOREIGN KEY (user_id) REFERENCES auth_users(id) ON DELETE CASCADE;

-- Step 6: Verify the fix
SELECT
    tc.constraint_name,
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS referenced_table,
    ccu.column_name AS referenced_column
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu
  ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.key_column_usage ccu
  ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
  AND tc.table_name = 'tenant_join_requests';

-- Step 7: Check for orphaned records
SELECT COUNT(*) as orphaned_count
FROM tenant_join_requests tjr
LEFT JOIN auth_users a ON tjr.user_id = a.id
WHERE a.id IS NULL;
