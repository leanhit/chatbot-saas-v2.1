-- ============================================================
-- V21: Add unique constraint on agents(user_id, tenant_id)
-- Ensures each user has at most one Agent profile per tenant.
-- Required for auto-provisioning idempotency.
-- ============================================================

-- Remove duplicate rows if any exist (keep the most recent one)
DELETE FROM agents a1
USING agents a2
WHERE a1.user_id IS NOT NULL
  AND a1.tenant_id IS NOT NULL
  AND a1.user_id = a2.user_id
  AND a1.tenant_id = a2.tenant_id
  AND a1.id < a2.id;

-- Add the unique constraint
ALTER TABLE agents
    ADD CONSTRAINT uq_agents_user_tenant UNIQUE (user_id, tenant_id);
