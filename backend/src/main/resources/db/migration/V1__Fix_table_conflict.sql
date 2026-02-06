-- Migration: Fix table name conflict between legacy auth and identity modules
-- This resolves Hibernate entity mapping conflict between Auth.java and User.java

-- Step 1: Rename legacy users table to auth_users
ALTER TABLE users RENAME TO auth_users;

-- Step 2: Update foreign key references in related tables
-- Note: These updates assume the foreign key columns reference the legacy users table

-- Update user_info table
ALTER TABLE user_info RENAME COLUMN user_id TO auth_user_id;
ALTER TABLE user_info 
ADD CONSTRAINT fk_user_info_auth_user 
FOREIGN KEY (auth_user_id) REFERENCES auth_users(id);

-- Update tenant_members table  
ALTER TABLE tenant_members RENAME COLUMN user_id TO auth_user_id;
ALTER TABLE tenant_members 
ADD CONSTRAINT fk_tenant_members_auth_user 
FOREIGN KEY (auth_user_id) REFERENCES auth_users(id);

-- Update tenant_join_requests table
ALTER TABLE tenant_join_requests RENAME COLUMN user_id TO auth_user_id;
ALTER TABLE tenant_join_requests 
ADD CONSTRAINT fk_tenant_join_requests_auth_user 
FOREIGN KEY (auth_user_id) REFERENCES auth_users(id);

-- Update address table for USER owner_type
UPDATE address SET owner_type = 'AUTH_USER' WHERE owner_type = 'USER';

-- Step 3: Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_auth_users_email ON auth_users(email);
CREATE INDEX IF NOT EXISTS idx_auth_users_status ON auth_users(status);
CREATE INDEX IF NOT EXISTS idx_auth_users_is_active ON auth_users(is_active);

-- Step 4: Verify identity tables are separate
-- Identity users table should already exist with unique email constraint
-- Identity user_credentials table should already exist with proper foreign keys
