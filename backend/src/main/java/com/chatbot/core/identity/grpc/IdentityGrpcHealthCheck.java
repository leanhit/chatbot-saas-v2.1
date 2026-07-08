package com.chatbot.core.identity.grpc;

import lombok.RequiredArgsConstructor;
import com.chatbot.core.identity.grpc.IdentityServiceOuterClass.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Identity gRPC Health Check
 *
 * Runs AFTER the entire Spring context has finished initializing (ApplicationReadyEvent)
 * to avoid bean singleton lock contention with userTransactionManager.
 */
@Component
@DependsOn("identityGrpcServer")
@Slf4j
public @RequiredArgsConstructor
class IdentityGrpcHealthCheck {

    @org.springframework.beans.factory.annotation.Value("${grpc.security.tls.enabled:false}")
    private boolean tlsEnabled;

    private final IdentityServiceGrpcImpl grpcService;

    @EventListener(ApplicationReadyEvent.class)
    public void performHealthCheck() {
        ManagedChannel channel = null;
        try {
            log.info("=== Bắt đầu Health Check cho Identity gRPC Service ===");

            ManagedChannelBuilder<?> builder = ManagedChannelBuilder.forAddress("localhost", 50051);
            if (tlsEnabled) {
                builder.useTransportSecurity();
            } else {
                builder.usePlaintext();
            }
            channel = builder.build();

            IdentityServiceGrpc.IdentityServiceBlockingStub blockingStub =
                    IdentityServiceGrpc.newBlockingStub(channel);

            // Test validateUser với user ID không tồn tại
            ValidateUserResponse validateResponse = blockingStub.validateUser(
                    ValidateUserRequest.newBuilder().setUserId("999").build());
            log.info("Validate User Response: valid={}, isActive={}, message={}",
                    validateResponse.getValid(),
                    validateResponse.getIsActive(),
                    validateResponse.getMessage());

            // Test isUserActive
            IsUserActiveResponse activeResponse = blockingStub.isUserActive(
                    IsUserActiveRequest.newBuilder().setUserId("999").build());
            log.info("Is User Active Response: userId={}, isActive={}, errorMessage={}",
                    activeResponse.getUserId(),
                    activeResponse.getIsActive(),
                    activeResponse.getErrorMessage());

            log.info("=== Identity gRPC Service Health Check Completed ===");

        } catch (Exception e) {
            log.error("Lỗi khi health check Identity gRPC", e);
        } finally {
            if (channel != null) {
                channel.shutdown();
            }
        }
    }
}
