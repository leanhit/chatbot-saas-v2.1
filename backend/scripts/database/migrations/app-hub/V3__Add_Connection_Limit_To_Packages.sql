-- Add connection_limit column to packages table
ALTER TABLE packages 
ADD COLUMN IF NOT EXISTS connection_limit INTEGER NOT NULL DEFAULT 1;

-- Add comment
COMMENT ON COLUMN packages.connection_limit IS 'Maximum number of connections allowed per package (1 for free, unlimited for paid packages)';

-- Update existing packages with appropriate connection limits
UPDATE packages 
SET connection_limit = CASE 
    WHEN package_id = 'free' THEN 1
    WHEN price > 0 THEN 2147483647 -- Unlimited for all paid packages
    ELSE 1
END
WHERE connection_limit = 1; -- Only update if still default

-- Create index for better performance
CREATE INDEX IF NOT EXISTS idx_packages_connection_limit ON packages(connection_limit);
