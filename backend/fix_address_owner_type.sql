-- FIX ADDRESS OWNER_TYPE VALUES
-- Update owner_type to properly distinguish between legacy auth and identity users

-- Step 1: Check current state
SELECT 'CURRENT_OWNER_TYPES' as step,
       owner_type,
       COUNT(*) as count
FROM address
GROUP BY owner_type
ORDER BY owner_type;

-- Step 2: Check which owner_ids reference which tables
SELECT 'OWNER_ID_REFERENCES' as step,
       'auth_users' as table_name,
       COUNT(*) as address_count
FROM address a
JOIN auth_users au ON a.owner_id = au.id
WHERE a.owner_type IN ('USER', 'AUTH_USER', 'LEGACY_USER')
GROUP BY 'auth_users'

UNION ALL

SELECT 'OWNER_ID_REFERENCES' as step,
       'users' as table_name,
       COUNT(*) as address_count
FROM address a
JOIN users u ON a.owner_id = u.id
WHERE a.owner_type IN ('USER', 'IDENTITY_USER', 'USER_IDENTITY')
GROUP BY 'users';

-- Step 3: Update owner_type based on actual references
-- Update addresses that reference auth_users table
UPDATE address 
SET owner_type = 'AUTH_USER'
WHERE owner_id IN (SELECT id FROM auth_users)
  AND owner_type IN ('USER', 'LEGACY_USER');

-- Update addresses that reference users table (Identity Hub)
UPDATE address 
SET owner_type = 'IDENTITY_USER'
WHERE owner_id IN (SELECT id FROM users)
  AND owner_type IN ('USER', 'USER_IDENTITY');

-- Step 4: Verify the updates
SELECT 'UPDATED_OWNER_TYPES' as step,
       owner_type,
       COUNT(*) as count
FROM address
GROUP BY owner_type
ORDER BY owner_type;

-- Step 5: Check for any remaining ambiguous USER type
SELECT 'REMAINING_AMBIGUOUS' as step,
       COUNT(*) as count
FROM address
WHERE owner_type = 'USER';

-- Step 6: Final verification - check data integrity
SELECT 'DATA_INTEGRITY_CHECK' as step,
       a.owner_type,
       COUNT(*) as total_addresses,
       COUNT(DISTINCT a.owner_id) as unique_owners
FROM address a
GROUP BY a.owner_type
ORDER BY a.owner_type;

-- Step 7: Check for orphaned addresses after updates
SELECT 'ORPHANED_AFTER_UPDATE' as step,
       owner_type,
       COUNT(*) as orphaned_count
FROM address a
LEFT JOIN auth_users au ON a.owner_id = au.id AND a.owner_type = 'AUTH_USER'
LEFT JOIN users u ON a.owner_id = u.id AND a.owner_type = 'IDENTITY_USER'
WHERE (a.owner_type = 'AUTH_USER' AND au.id IS NULL)
   OR (a.owner_type = 'IDENTITY_USER' AND u.id IS NULL)
   OR (a.owner_type NOT IN ('AUTH_USER', 'IDENTITY_USER'))
GROUP BY a.owner_type
ORDER BY owner_type;
