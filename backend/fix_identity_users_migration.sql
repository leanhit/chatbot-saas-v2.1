-- FIX IDENTITY_USERS TABLE MIGRATION
-- This script fixes the foreign key constraint issue with user_credentials

-- Step 1: Drop existing foreign key constraint on user_credentials
ALTER TABLE user_credentials DROP CONSTRAINT IF EXISTS fkd7eugde06ldy8dwemn1m8geev;

-- Step 2: Create identity_users table with same structure as users
CREATE TABLE IF NOT EXISTS identity_users (LIKE users INCLUDING ALL);

-- Step 3: Migrate data from users to identity_users (only if identity_users is empty)
INSERT INTO identity_users 
SELECT * FROM users 
WHERE NOT EXISTS (
    SELECT 1 FROM identity_users 
    LIMIT 1
);

-- Step 4: Update foreign key in user_credentials to reference identity_users
ALTER TABLE user_credentials 
ADD CONSTRAINT fk_user_credentials_identity_user 
FOREIGN KEY (user_id) REFERENCES identity_users(id);

-- Step 5: Verify data
SELECT 'IDENTITY_USERS_RECORD_COUNT' as audit_type, COUNT(*) as record_count FROM identity_users;
SELECT 'USER_CREDENTIALS_RECORD_COUNT' as audit_type, COUNT(*) as record_count FROM user_credentials;

-- Step 6: Show sample data
SELECT 'IDENTITY_USERS_SAMPLE_DATA' as audit_type, 
       id, email, status, created_at 
FROM identity_users 
LIMIT 3;
