-- Update Tenants Table Schema to match Entity
-- Change id from UUID to BIGINT and add tenantkey column

-- Step 1: Add new id column as BIGINT
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS id_new BIGINT;

-- Step 2: Populate id_new with sequential values (since UUID can't be converted to BIGINT)
-- This will assign new sequential IDs to existing records
UPDATE tenants SET id_new = (row_number() OVER (ORDER BY created_at));

-- Step 3: Drop foreign key constraints that reference the old id column
-- Note: You may need to add ALTER TABLE statements to drop constraints if they exist
-- ALTER TABLE tenant_members DROP CONSTRAINT IF EXISTS fk_tenant_members_tenant_id;
-- ALTER TABLE tenant_profiles DROP CONSTRAINT IF EXISTS fk_tenant_profiles_tenant_id;
-- ALTER TABLE tenant_professionals DROP CONSTRAINT IF EXISTS fk_tenant_professionals_tenant_id;

-- Step 4: Drop the old id column
ALTER TABLE tenants DROP COLUMN id;

-- Step 5: Rename id_new to id
ALTER TABLE tenants RENAME COLUMN id_new TO id;

-- Step 6: Set id as PRIMARY KEY
ALTER TABLE tenants ADD PRIMARY KEY (id);

-- Step 7: Add tenantkey column
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS tenantkey VARCHAR(255) UNIQUE NOT NULL DEFAULT '';

-- Step 8: Populate tenantkey with UUID values for existing records
UPDATE tenants SET tenantkey = gen_random_uuid()::text WHERE tenantkey = '';

-- Step 9: Add index on tenantkey
CREATE INDEX IF NOT EXISTS idx_tenant_key ON tenants(tenantkey);

-- Step 10: Recreate foreign key constraints with the new id column type
-- ALTER TABLE tenant_members ADD CONSTRAINT fk_tenant_members_tenant_id 
--     FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE;
-- ALTER TABLE tenant_profiles ADD CONSTRAINT fk_tenant_profiles_tenant_id 
--     FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE;
-- ALTER TABLE tenant_professionals ADD CONSTRAINT fk_tenant_professionals_tenant_id 
--     FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE;
