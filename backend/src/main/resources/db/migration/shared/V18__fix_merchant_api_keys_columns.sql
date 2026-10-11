-- ============================================================
-- V18: Align merchant_api_keys table columns with JPA entity
-- ============================================================

ALTER TABLE merchant_api_keys ADD COLUMN IF NOT EXISTS api_key VARCHAR(255);
ALTER TABLE merchant_api_keys ADD COLUMN IF NOT EXISTS api_secret VARCHAR(255);
ALTER TABLE merchant_api_keys ADD COLUMN IF NOT EXISTS description VARCHAR(500);
ALTER TABLE merchant_api_keys ADD COLUMN IF NOT EXISTS created_by BIGINT;

ALTER TABLE merchant_api_keys ALTER COLUMN merchant_id DROP NOT NULL;
ALTER TABLE merchant_api_keys ALTER COLUMN api_key_hash DROP NOT NULL;
ALTER TABLE merchant_api_keys ALTER COLUMN api_secret_hash DROP NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_merchant_api_keys_api_key ON merchant_api_keys(api_key);
