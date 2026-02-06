-- Add locale field to identity_users table
ALTER TABLE identity_users 
ADD COLUMN locale VARCHAR(10) NOT NULL DEFAULT 'vi' COMMENT 'User locale preference (e.g., vi, en)';

-- Add default_locale field to tenants table  
ALTER TABLE tenants
ADD COLUMN default_locale VARCHAR(10) NOT NULL DEFAULT 'vi' COMMENT 'Tenant default locale (e.g., vi, en)';

-- Create index for locale field for better query performance
CREATE INDEX idx_identity_users_locale ON identity_users(locale);
CREATE INDEX idx_tenants_default_locale ON tenants(default_locale);
