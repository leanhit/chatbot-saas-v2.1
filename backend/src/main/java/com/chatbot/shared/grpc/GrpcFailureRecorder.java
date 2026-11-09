package com.chatbot.shared.grpc;

import io.grpc.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Best-effort recorder for failed gRPC calls.
 * Never throws — recording failures must not affect call handling.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GrpcFailureRecorder {

    private final GrpcCallFailureRepository repository;

    public void record(String method, Status status) {
        try {
            GrpcCallFailure failure = GrpcCallFailure.builder()
                    .method(method)
                    .statusCode(status.getCode().name())
                    .statusDescription(status.getDescription())
                    .build();
            repository.save(failure);
        } catch (Exception e) {
            log.warn("Failed to record gRPC call failure for {}: {}", method, e.getMessage());
        }
    }
}
