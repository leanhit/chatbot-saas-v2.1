-- Add balance constraints to prevent negative values
-- This migration adds database-level protection for balance integrity

-- Add check constraint to prevent negative balance
ALTER TABLE users 
ADD CONSTRAINT chk_balance_non_negative 
CHECK (balance >= 0.00);

-- Add index for better performance on balance queries
CREATE INDEX IF NOT EXISTS idx_users_balance ON users(balance);

-- Add comment for documentation
COMMENT ON CONSTRAINT chk_balance_non_negative ON users IS 'Prevents negative balance values';
