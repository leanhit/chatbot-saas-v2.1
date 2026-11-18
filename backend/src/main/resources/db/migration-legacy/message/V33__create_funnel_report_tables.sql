-- Phase 5: Funnel stages, custom reports, report schedules (tenant-scoped)
CREATE TABLE IF NOT EXISTS funnel_stage (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT,
    tenant_key VARCHAR(255),
    name VARCHAR(100) NOT NULL,
    stage_order INTEGER NOT NULL,
    match_type VARCHAR(20) NOT NULL,
    match_value VARCHAR(50),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_funnel_stage_tenant ON funnel_stage(tenant_id, active);                              
CREATE TABLE IF NOT EXISTS custom_report (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT,
    tenant_key VARCHAR(255),
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    report_type VARCHAR(20) NOT NULL,
    date_range_days INTEGER NOT NULL DEFAULT 30,
    filters JSONB,
    last_generated_at TIMESTAMP,
    last_result JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_custom_report_tenant ON custom_report(tenant_id, is_deleted);                        
CREATE TABLE IF NOT EXISTS report_schedule (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT,
    tenant_key VARCHAR(255),
    report_id BIGINT NOT NULL,
    cron_expression VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    next_run_at TIMESTAMP,
    last_run_at TIMESTAMP,
    last_run_status VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_report_schedule_due ON report_schedule(enabled, next_run_at);
