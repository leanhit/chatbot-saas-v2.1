-- ============================================================
-- V22: Fix agents.email unique constraint
--
-- Problem: email had a global UNIQUE constraint, causing
--          DuplicateKeyException when the same user joined
--          multiple tenants (each join creates a new Agent row).
--
-- Solution: Drop the global unique on email,
--           replace with composite unique (email, tenant_id).
--           The (user_id, tenant_id) unique from V21 is kept.
-- ============================================================

-- 1. Drop the old global unique constraint / index on email
--    (created by V1 as: email VARCHAR(255) NOT NULL UNIQUE)
ALTER TABLE agents
    DROP CONSTRAINT IF EXISTS agents_email_key;

-- Also drop by index name in case it was created as an index
DROP INDEX IF EXISTS agents_email_key;

-- 2. Add composite unique: one agent email per tenant
ALTER TABLE agents
    ADD CONSTRAINT uq_agent_email_tenant UNIQUE (email, tenant_id);
