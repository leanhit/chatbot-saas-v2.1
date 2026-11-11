-- V19: Failed gRPC call records (DLQ for gRPC)
CREATE TABLE IF NOT EXISTS grpc_call_failures (
    id BIGSERIAL PRIMARY KEY,
    method VARCHAR(512) NOT NULL,
    status_code VARCHAR(32) NOT NULL,
    status_description VARCHAR(1024),
    tenant_id BIGINT,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_grpc_fail_method ON grpc_call_failures(method);
CREATE INDEX IF NOT EXISTS idx_grpc_fail_occurred ON grpc_call_failures(occurred_at);
