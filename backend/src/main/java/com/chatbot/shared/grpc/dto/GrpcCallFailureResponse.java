package com.chatbot.shared.grpc.dto;

import com.chatbot.shared.infrastructure.model.GrpcCallFailure;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrpcCallFailureResponse {
    private Long id;
    private String method;
    private String statusCode;
    private String statusDescription;
    private Long tenantId;
    private LocalDateTime occurredAt;

    public static GrpcCallFailureResponse from(GrpcCallFailure f) {
        if (f == null) return null;
        return GrpcCallFailureResponse.builder()
                .id(f.getId()).method(f.getMethod()).statusCode(f.getStatusCode())
                .statusDescription(f.getStatusDescription()).tenantId(f.getTenantId())
                .occurredAt(f.getOccurredAt())
                .build();
    }
}
