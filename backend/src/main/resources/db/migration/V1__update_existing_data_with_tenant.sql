-- Update existing records with default tenant values
-- This migration handles existing data that doesn't have tenant information

-- Update file_metadata table
UPDATE file_metadata 
SET tenant_id = COALESCE(tenant_id, 1)
WHERE tenant_id IS NULL;

-- Update category table  
UPDATE category 
SET tenant_id = COALESCE(tenant_id, 1) 
WHERE tenant_id IS NULL;

-- Update facebook_connection table
UPDATE facebook_connection 
SET tenant_id = COALESCE(tenant_id, 1)
WHERE tenant_id IS NULL;

-- Update conversation table
UPDATE conversation 
SET tenant_id = COALESCE(tenant_id, 1)
WHERE tenant_id IS NULL;

-- Update message table
UPDATE message 
SET tenant_id = COALESCE(tenant_id, 1)
WHERE tenant_id IS NULL;

-- Update facebook_user table
UPDATE facebook_users 
SET tenant_id = COALESCE(tenant_id, 1)
WHERE tenant_id IS NULL;

-- Update user_id_mapping table
UPDATE user_id_mapping 
SET tenant_id = COALESCE(tenant_id, 1)
WHERE tenant_id IS NULL;

-- Update fb_customer_staging table
UPDATE fb_customer_staging 
SET tenant_id = COALESCE(tenant_id, 1)
WHERE tenant_id IS NULL;

-- Update fb_captured_phone table
UPDATE fb_captured_phone 
SET tenant_id = COALESCE(tenant_id, 1)
WHERE tenant_id IS NULL;
