-- Idempotency key for webhook deliveries (P2-B)
-- Stable across retries so receivers can deduplicate.
ALTER TABLE tenant_webhook_deliveries
    ADD COLUMN IF NOT EXISTS delivery_key VARCHAR(64);

UPDATE tenant_webhook_deliveries
SET delivery_key = gen_random_uuid()::text
WHERE delivery_key IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_tenant_webhook_deliveries_key
    ON tenant_webhook_deliveries(delivery_key);
