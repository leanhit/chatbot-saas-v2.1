-- ============================================================
-- V22: Add missing tenant_id foreign key constraints
-- ============================================================

-- Add foreign key constraint to penny_escalation_tickets
ALTER TABLE penny_escalation_tickets 
ADD CONSTRAINT fk_escalation_tenant 
FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE;

-- Add foreign key constraint to penny_knowledge_articles
ALTER TABLE penny_knowledge_articles 
ADD CONSTRAINT fk_knowledge_tenant 
FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE;
