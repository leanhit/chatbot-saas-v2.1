-- ============================================================
-- V12: Add is_read column to messages table if it doesn't exist
-- ============================================================

-- Add is_read column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'messages' 
        AND column_name = 'is_read'
    ) THEN
        ALTER TABLE messages ADD COLUMN is_read BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;
END $$;

-- Add comment to the column
COMMENT ON COLUMN messages.is_read IS 'Indicates whether the message has been read by the recipient';
