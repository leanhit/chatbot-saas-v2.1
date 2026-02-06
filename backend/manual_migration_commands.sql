-- Manual migration commands to fix FK mismatches
-- Run these manually in PostgreSQL to fix the foreign key issues

-- Step 1: Check current state
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
WHERE tc.table_name = 'user_info'
  AND tc.constraint_type = 'FOREIGN KEY';

-- Step 2: Drop existing FK constraint if it exists
ALTER TABLE user_info DROP CONSTRAINT IF EXISTS fkbvfqse8k5w3yc5igairr1g2hc;

-- Step 3: Check if column name needs to be fixed
SELECT column_name 
FROM information_schema.columns 
WHERE table_name = 'user_info' AND column_name LIKE '%user%';

-- Step 4: If column is named auth_user_id, rename it to user_id
-- Uncomment the line below if needed:
-- ALTER TABLE user_info RENAME COLUMN auth_user_id TO user_id;

-- Step 5: Add proper foreign key constraint
ALTER TABLE user_info 
ADD CONSTRAINT fk_user_info_auth_user 
FOREIGN KEY (user_id) REFERENCES auth_users(id) ON DELETE CASCADE;

-- Step 6: Verify the fix
SELECT
    tc.constraint_name,
    kcu.column_name,
    ccu.table_name AS referenced_table,
    ccu.column_name AS referenced_column
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu
  ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage ccu
  ON ccu.constraint_name = tc.constraint_name
WHERE tc.table_name = 'user_info'
  AND tc.constraint_type = 'FOREIGN KEY';

-- Step 7: Check for orphaned records
SELECT COUNT(*) as orphaned_count
FROM user_info ui
LEFT JOIN auth_users a ON ui.user_id = a.id
WHERE a.id IS NULL;
