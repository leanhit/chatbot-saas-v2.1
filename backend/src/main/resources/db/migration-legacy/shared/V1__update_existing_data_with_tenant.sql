-- Update existing records with default tenant values
-- This migration handles existing data that doesn't have tenant information
-- Note: Only updates tables that exist in the shared database

DO $$
BEGIN
    -- Update file_metadata table if it exists
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'file_metadata') THEN
        UPDATE file_metadata 
        SET tenant_id = COALESCE(tenant_id, 1)
        WHERE tenant_id IS NULL;
    END IF;

    -- Update category table if it exists
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'category') THEN
        UPDATE category 
        SET tenant_id = COALESCE(tenant_id, 1) 
        WHERE tenant_id IS NULL;
    END IF;

    -- Update facebook_connection table if it exists
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'facebook_connection') THEN
        UPDATE facebook_connection 
        SET tenant_id = COALESCE(tenant_id, 1)
        WHERE tenant_id IS NULL;
    END IF;

    -- Update conversation table if it exists
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'conversation') THEN
        UPDATE conversation 
        SET tenant_id = COALESCE(tenant_id, 1)
        WHERE tenant_id IS NULL;
    END IF;

    -- Update message table if it exists
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'message') THEN
        UPDATE message 
        SET tenant_id = COALESCE(tenant_id, 1)
        WHERE tenant_id IS NULL;
    END IF;

    -- Update facebook_user table if it exists
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'facebook_users') THEN
        UPDATE facebook_users 
        SET tenant_id = COALESCE(tenant_id, 1)
        WHERE tenant_id IS NULL;
    END IF;

    -- Update user_id_mapping table if it exists
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'user_id_mapping') THEN
        UPDATE user_id_mapping 
        SET tenant_id = COALESCE(tenant_id, 1)
        WHERE tenant_id IS NULL;
    END IF;

    -- Update fb_customer_staging table if it exists
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'fb_customer_staging') THEN
        UPDATE fb_customer_staging 
        SET tenant_id = COALESCE(tenant_id, 1)
        WHERE tenant_id IS NULL;
    END IF;

    -- Update fb_captured_phone table if it exists
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'fb_captured_phone') THEN
        UPDATE fb_captured_phone 
        SET tenant_id = COALESCE(tenant_id, 1)
        WHERE tenant_id IS NULL;
    END IF;
END $$;
