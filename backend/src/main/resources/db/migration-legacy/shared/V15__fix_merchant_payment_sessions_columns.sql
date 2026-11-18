-- ============================================================
-- V15: Fix column names in merchant_payment_sessions table
-- ============================================================

DO $$
BEGIN
    -- Check if table exists and column needs renaming
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'merchant_payment_sessions') THEN
        -- Rename columns from camelCase to snake_case if they exist
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'merchant_payment_sessions' AND column_name = 'cancelurl') THEN
            ALTER TABLE merchant_payment_sessions RENAME COLUMN cancelurl TO cancel_url;
        END IF;
        
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'merchant_payment_sessions' AND column_name = 'returnurl') THEN
            ALTER TABLE merchant_payment_sessions RENAME COLUMN returnurl TO return_url;
        END IF;
        
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'merchant_payment_sessions' AND column_name = 'paymentcode') THEN
            ALTER TABLE merchant_payment_sessions RENAME COLUMN paymentcode TO payment_reference_code;
        END IF;
        
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'merchant_payment_sessions' AND column_name = 'expiredat') THEN
            ALTER TABLE merchant_payment_sessions RENAME COLUMN expiredat TO expires_at;
        END IF;
    END IF;
END $$;
