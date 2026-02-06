-- CREATE IDENTITY_USERS TABLE AND MIGRATE DATA
-- This script creates the identity_users table and migrates data from users table

-- Step 1: Create identity_users table with same structure as users
CREATE TABLE identity_users (LIKE users INCLUDING ALL);

-- Step 2: Migrate all data from users to identity_users
INSERT INTO identity_users SELECT * FROM users;

-- Step 3: Verify data migration
SELECT 'IDENTITY_USERS_RECORD_COUNT' as audit_type, COUNT(*) as record_count FROM identity_users;

-- Step 4: Show sample data to verify
SELECT 'IDENTITY_USERS_SAMPLE_DATA' as audit_type, 
       id, 
       email, 
       status, 
       created_at, 
       updated_at 
FROM identity_users 
LIMIT 3;

-- Step 5: Compare with original users table
SELECT 'USERS_RECORD_COUNT' as audit_type, COUNT(*) as record_count FROM users;
