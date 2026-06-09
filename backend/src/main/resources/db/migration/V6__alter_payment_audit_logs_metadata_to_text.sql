-- V6__alter_payment_audit_logs_metadata_to_text.sql
-- Alter metadata column type to TEXT for compatibility with string storage
ALTER TABLE payment_audit_logs
    ALTER COLUMN metadata TYPE TEXT USING metadata::text;
