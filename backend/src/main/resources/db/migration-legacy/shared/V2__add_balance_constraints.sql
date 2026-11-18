-- Add balance constraints to prevent negative values
-- This migration adds database-level protection for balance integrity

DO $$
BEGIN
    -- Only alter users table if it exists
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'users') THEN
        -- Add check constraint to prevent negative balance if not exists
        IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_balance_non_negative') THEN
            ALTER TABLE users 
            ADD CONSTRAINT chk_balance_non_negative 
            CHECK (balance >= 0.00);
            
            COMMENT ON CONSTRAINT chk_balance_non_negative ON users IS 'Prevents negative balance values';
        END IF;

        -- Add index for better performance on balance queries
        IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_users_balance') THEN
            CREATE INDEX idx_users_balance ON users(balance);
        END IF;
    END IF;
END $$;
