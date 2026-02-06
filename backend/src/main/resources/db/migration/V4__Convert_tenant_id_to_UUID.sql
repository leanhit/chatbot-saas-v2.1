-- Migration V4: Convert tenant_id columns from BIGINT to UUID
-- This migration updates the tenant system to use UUID consistently across all tables

-- =====================================================
-- STEP 1: Create backup tables before migration
-- =====================================================

-- Backup user_tenants table
CREATE TABLE user_tenants_backup AS TABLE user_tenants;

-- Check if there are any entities using BaseTenantEntity
-- (These would have tenant_id columns that need conversion)

-- =====================================================
-- STEP 2: Add new UUID columns
-- =====================================================

-- Add new UUID column to user_tenants
ALTER TABLE user_tenants 
ADD COLUMN tenant_id_uuid UUID;

-- =====================================================
-- STEP 3: Migrate data from BIGINT to UUID
-- =====================================================

-- Convert existing tenant_id values to UUIDs
-- Note: This assumes existing tenant_id values correspond to Tenant.id values
-- We need to map from the existing tenants table
UPDATE user_tenants ut
SET tenant_id_uuid = t.id
FROM tenants t
WHERE ut.tenant_id = t.id::bigint;

-- Handle any NULL values (if any)
UPDATE user_tenants 
SET tenant_id_uuid = gen_random_uuid()
WHERE tenant_id_uuid IS NULL AND tenant_id IS NOT NULL;

-- =====================================================
-- STEP 4: Drop old columns and rename new ones
-- =====================================================

-- Drop the old BIGINT column
ALTER TABLE user_tenants DROP COLUMN tenant_id;

-- Rename the UUID column
ALTER TABLE user_tenants RENAME COLUMN tenant_id_uuid TO tenant_id;

-- Make the new column NOT NULL
ALTER TABLE user_tenants ALTER COLUMN tenant_id SET NOT NULL;

-- =====================================================
-- STEP 5: Update foreign key constraints
-- =====================================================

-- Drop existing foreign key constraint if it exists
ALTER TABLE user_tenants DROP CONSTRAINT IF EXISTS user_tenants_tenant_id_fkey;

-- Add new foreign key constraint with UUID
ALTER TABLE user_tenants 
ADD CONSTRAINT user_tenants_tenant_id_fkey 
FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE;

-- =====================================================
-- STEP 6: Update indexes
-- =====================================================

-- Create index on the new UUID column
CREATE INDEX IF NOT EXISTS idx_user_tenants_tenant_id ON user_tenants(tenant_id);

-- Recreate unique constraint if needed
ALTER TABLE user_tenants 
DROP CONSTRAINT IF EXISTS user_tenants_user_id_tenant_id_key;

ALTER TABLE user_tenants 
ADD CONSTRAINT user_tenants_user_id_tenant_id_key 
UNIQUE (user_id, tenant_id);

-- =====================================================
-- STEP 7: Handle other tables with tenant_id (if any)
-- =====================================================

-- Check for other tables that might have tenant_id columns
-- This is a safety check for any entities extending BaseTenantEntity

-- Example for address table (if it exists and uses BaseTenantEntity)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'address' AND column_name = 'tenant_id') THEN
        -- Add UUID column
        ALTER TABLE address ADD COLUMN tenant_id_uuid UUID;
        
        -- Migrate data
        UPDATE address a
        SET tenant_id_uuid = t.id
        FROM tenants t
        WHERE a.tenant_id = t.id::bigint;
        
        -- Handle NULLs
        UPDATE address 
        SET tenant_id_uuid = gen_random_uuid()
        WHERE tenant_id_uuid IS NULL AND tenant_id IS NOT NULL;
        
        -- Drop old column
        ALTER TABLE address DROP COLUMN tenant_id;
        ALTER TABLE address RENAME COLUMN tenant_id_uuid TO tenant_id;
        ALTER TABLE address ALTER COLUMN tenant_id SET NOT NULL;
        
        -- Update constraints
        ALTER TABLE address 
        ADD CONSTRAINT address_tenant_id_fkey 
        FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE;
        
        -- Update indexes
        CREATE INDEX IF NOT EXISTS idx_address_tenant_id ON address(tenant_id);
    END IF;
END $$;

-- =====================================================
-- STEP 8: Verification
-- =====================================================

-- Verify data migration
SELECT 
    'user_tenants' as table_name,
    COUNT(*) as total_records,
    COUNT(tenant_id) as records_with_tenant_id,
    COUNT(DISTINCT tenant_id) as unique_tenants
FROM user_tenants;

-- Verify referential integrity
SELECT 
    'orphaned_user_tenants' as check_name,
    COUNT(*) as count
FROM user_tenants ut
LEFT JOIN tenants t ON ut.tenant_id = t.id
WHERE t.id IS NULL;

-- =====================================================
-- STEP 9: Cleanup
-- =====================================================

-- Keep backup table for safety (comment out if not needed)
-- DROP TABLE user_tenants_backup;

-- Update any sequences if needed (shouldn't be necessary for UUID)

-- =====================================================
-- STEP 10: Update any application-level constants
-- =====================================================

-- Note: This migration only handles database changes
-- Application code changes must be done separately:
-- - Update entity classes to use UUID
-- - Update repository methods
-- - Update service classes
-- - Update JWT token handling

COMMIT;

-- Add verification comments
COMMENT ON TABLE user_tenants IS 'User-tenant mapping with UUID tenant_id (v0.2)';
COMMENT ON COLUMN user_tenants.tenant_id IS 'UUID reference to tenants table (converted from BIGINT)';
