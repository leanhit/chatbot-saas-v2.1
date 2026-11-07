-- Phase 6: GDPR data deletion requests
CREATE TABLE IF NOT EXISTS data_deletion_requests (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    tenant_key VARCHAR(255),
    requested_by VARCHAR(255) NOT NULL,
    reason TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    scheduled_at TIMESTAMP,
    processed_at TIMESTAMP,
    processed_by VARCHAR(255),
    result_summary TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_deletion_req_tenant ON data_deletion_requests(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_deletion_req_due ON data_deletion_requests(status, scheduled_at);
