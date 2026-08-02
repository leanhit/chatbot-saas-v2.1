-- Create discounts table
CREATE TABLE IF NOT EXISTS discounts (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    discount_type VARCHAR(20) NOT NULL,
    discount_value DECIMAL(10,2) NOT NULL,
    minimum_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    maximum_discount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    usage_limit INTEGER NOT NULL DEFAULT 0,
    usage_count INTEGER NOT NULL DEFAULT 0,
    usage_limit_per_user INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    valid_from TIMESTAMP NOT NULL,
    valid_until TIMESTAMP NOT NULL,
    description TEXT,
    applicable_package_id VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create discount_user_usage table for tracking which users have used each discount
CREATE TABLE IF NOT EXISTS discount_user_usage (
    discount_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (discount_id, user_id),
    CONSTRAINT fk_discount_user_usage_discount FOREIGN KEY (discount_id) REFERENCES discounts(id) ON DELETE CASCADE
);

-- Add missing columns if they don't exist (for backwards compatibility)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'discounts' AND column_name = 'is_active') THEN
        ALTER TABLE discounts ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT true;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'discounts' AND column_name = 'valid_from') THEN
        ALTER TABLE discounts ADD COLUMN valid_from TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'discounts' AND column_name = 'valid_until') THEN
        ALTER TABLE discounts ADD COLUMN valid_until TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'discounts' AND column_name = 'applicable_package_id') THEN
        ALTER TABLE discounts ADD COLUMN applicable_package_id VARCHAR(50);
    END IF;
END $$;

-- Create indexes for performance (only if columns exist)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'discounts' AND column_name = 'code') THEN
        CREATE INDEX IF NOT EXISTS idx_discounts_code ON discounts(code);
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'discounts' AND column_name = 'is_active') THEN
        CREATE INDEX IF NOT EXISTS idx_discounts_is_active ON discounts(is_active);
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'discounts' AND column_name = 'valid_from') 
       AND EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'discounts' AND column_name = 'valid_until') THEN
        CREATE INDEX IF NOT EXISTS idx_discounts_valid_period ON discounts(valid_from, valid_until);
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'discounts' AND column_name = 'applicable_package_id') THEN
        CREATE INDEX IF NOT EXISTS idx_discounts_applicable_package ON discounts(applicable_package_id);
    END IF;
END $$;
