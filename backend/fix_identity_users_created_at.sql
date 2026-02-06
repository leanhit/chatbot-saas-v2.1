-- FIX created_at NULL VALUES IN identity_users TABLE
-- This script updates null created_at and updated_at columns

-- Step 1: Update NULL created_at values to current timestamp
UPDATE identity_users 
SET created_at = CURRENT_TIMESTAMP 
WHERE created_at IS NULL;

-- Step 2: Update NULL updated_at values to current timestamp  
UPDATE identity_users 
SET updated_at = CURRENT_TIMESTAMP 
WHERE updated_at IS NULL;

-- Step 3: Verify the fix
SELECT 'IDENTITY_USERS_CREATED_AT_FIX' as audit_type,
       COUNT(*) as total_records,
       COUNT(CASE WHEN created_at IS NOT NULL THEN 1 END) as records_with_created_at,
       COUNT(CASE WHEN updated_at IS NOT NULL THEN 1 END) as records_with_updated_at
FROM identity_users;

-- Step 4: Show sample data to verify
SELECT 'IDENTITY_USERS_SAMPLE_DATA' as audit_type,
       id,
       email,
       status,
       created_at,
       updated_at
FROM identity_users
LIMIT 3;
