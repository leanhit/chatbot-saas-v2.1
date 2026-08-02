-- Add balance constraints to prevent negative values
-- This migration adds database-level protection for balance integrity

DO $$
BEGIN
    -- Add check constraint to prevent negative balance if not exists
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_balance_non_negative') THEN
        ALTER TABLE users 
        ADD CONSTRAINT chk_balance_non_negative 
        CHECK (balance >= 0.00);
    END IF;
END $$;

-- Add index for better performance on balance queries
CREATE INDEX IF NOT EXISTS idx_users_balance ON users(balance);

-- Add comment for documentation
COMMENT ON CONSTRAINT chk_balance_non_negative ON users IS 'Prevents negative balance values';
