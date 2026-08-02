-- ============================================================
-- V8: Create Routing, SLA, Agent Management Tables
-- Phase 1.3: Attribute-based Routing (routing_rules)
-- Phase 2.1: SLA Monitoring (sla_configurations)
-- Phase 2.2: Multi-tier Escalation (escalation_tiers)
-- Phase 3.1: Agent Management (agents)
-- Phase 3.2: Skills-based Routing (skills)
-- ============================================================

-- ============================================================
-- Table: agents
-- ============================================================
CREATE TABLE IF NOT EXISTS agents (
    id                           BIGSERIAL PRIMARY KEY,
    tenant_id                    BIGINT       NOT NULL,
    user_id                      BIGINT,
    name                         VARCHAR(255) NOT NULL,
    email                        VARCHAR(255) NOT NULL UNIQUE,
    role                         VARCHAR(50)  NOT NULL DEFAULT 'AGENT',
    status                       VARCHAR(50)  NOT NULL DEFAULT 'OFFLINE',
    current_load                 INTEGER      NOT NULL DEFAULT 0,
    max_concurrent_conversations INTEGER      NOT NULL DEFAULT 10,
    skills                       TEXT,              -- JSON array of skill strings
    assignment_preferences       TEXT,              -- JSON object
    active                       BOOLEAN      NOT NULL DEFAULT TRUE,
    bio                          TEXT,
    phone_number                 VARCHAR(50),
    avatar_url                   TEXT,
    last_activity_at             TIMESTAMP,
    created_at                   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at                   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_agent_tenant ON agents (tenant_id);
CREATE INDEX IF NOT EXISTS idx_agent_status ON agents (status);
CREATE INDEX IF NOT EXISTS idx_agent_user   ON agents (user_id);
CREATE INDEX IF NOT EXISTS idx_agent_active ON agents (tenant_id, active, status);

-- ============================================================
-- Table: skills
-- ============================================================
CREATE TABLE IF NOT EXISTS skills (
    id          BIGSERIAL PRIMARY KEY,
    tenant_id   BIGINT       NOT NULL,
    name        VARCHAR(255) NOT NULL,
    category    VARCHAR(50)  NOT NULL DEFAULT 'SOFT_SKILL',
    description TEXT,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    level       VARCHAR(50),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_skill_tenant   ON skills (tenant_id);
CREATE INDEX IF NOT EXISTS idx_skill_category ON skills (category);
CREATE INDEX IF NOT EXISTS idx_skill_active   ON skills (tenant_id, active);

-- ============================================================
-- Table: routing_rules
-- ============================================================
CREATE TABLE IF NOT EXISTS routing_rules (
    id          BIGSERIAL PRIMARY KEY,
    tenant_id   BIGINT       NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    priority    INTEGER      NOT NULL DEFAULT 0,
    conditions  TEXT,              -- JSON object
    action      TEXT,              -- JSON object
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    rule_type   VARCHAR(50)  NOT NULL DEFAULT 'AUTO_ASSIGN',
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_routing_rule_tenant   ON routing_rules (tenant_id);
CREATE INDEX IF NOT EXISTS idx_routing_rule_priority ON routing_rules (priority);
CREATE INDEX IF NOT EXISTS idx_routing_rule_active   ON routing_rules (tenant_id, active);

-- ============================================================
-- Table: sla_configurations
-- ============================================================
CREATE TABLE IF NOT EXISTS sla_configurations (
    id                     BIGSERIAL PRIMARY KEY,
    tenant_id              BIGINT       NOT NULL,
    customer_tier          VARCHAR(100) NOT NULL,
    expected_response_time BIGINT       NOT NULL,  -- seconds
    max_breach_count       INTEGER      NOT NULL DEFAULT 3,
    active                 BOOLEAN      NOT NULL DEFAULT TRUE,
    description            TEXT,
    created_at             TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_sla_config_tenant ON sla_configurations (tenant_id);
CREATE INDEX IF NOT EXISTS idx_sla_config_tier   ON sla_configurations (customer_tier);
CREATE INDEX IF NOT EXISTS idx_sla_config_active ON sla_configurations (tenant_id, active);

-- ============================================================
-- Table: escalation_tiers
-- ============================================================
CREATE TABLE IF NOT EXISTS escalation_tiers (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT       NOT NULL,
    level           INTEGER      NOT NULL,
    name            VARCHAR(255) NOT NULL,
    timeout_seconds BIGINT       NOT NULL,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    description     TEXT,
    required_role   VARCHAR(50),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_escalation_tier_tenant ON escalation_tiers (tenant_id);
CREATE INDEX IF NOT EXISTS idx_escalation_tier_level  ON escalation_tiers (level);
CREATE INDEX IF NOT EXISTS idx_escalation_tier_active ON escalation_tiers (tenant_id, active);

-- ============================================================
-- Default data: seed default escalation tiers for all existing tenants
-- New tenants will get these via code (SLAMonitorService.createDefaultSLAConfigurations)
-- ============================================================
-- (intentionally left empty - seeding is handled in Java service layer on first use)
