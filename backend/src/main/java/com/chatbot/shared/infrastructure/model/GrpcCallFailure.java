package com.chatbot.shared.infrastructure.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Record of a failed gRPC call (non-OK status) for later inspection/replay.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "grpc_call_failures",
    indexes = {
        @Index(name = "idx_grpc_fail_method", columnList = "method"),
        @Index(name = "idx_grpc_fail_occurred", columnList = "occurred_at")
    }
)
public class GrpcCallFailure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "method", nullable = false, length = 512)
    private String method;

    @Column(name = "status_code", nullable = false, length = 32)
    private String statusCode;

    @Column(name = "status_description", length = 1024)
    private String statusDescription;

    @Column(name = "tenant_id")
    private Long tenantId;

    @CreationTimestamp
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private LocalDateTime occurredAt;
}
