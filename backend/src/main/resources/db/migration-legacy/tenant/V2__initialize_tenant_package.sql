-- Initialize existing tenants with free package in Tenant database
DO $$
BEGIN
    UPDATE tenants 
    SET current_package_id = COALESCE(current_package_id, 'free'),
        package_activated_at = COALESCE(package_activated_at, CURRENT_TIMESTAMP)
    WHERE current_package_id IS NULL;
    
    RAISE NOTICE 'Initialized existing tenants with free package in tenant DB';
END $$;
