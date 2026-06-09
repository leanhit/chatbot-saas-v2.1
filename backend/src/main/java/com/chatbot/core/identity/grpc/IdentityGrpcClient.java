package com.chatbot.core.identity.grpc;

import com.chatbot.core.identity.grpc.IdentityServiceOuterClass.*;
import com.chatbot.core.identity.grpc.IdentityServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@DependsOn("identityGrpcServer")
@Slf4j
public class IdentityGrpcClient {

    private ManagedChannel channel;
    private IdentityServiceGrpc.IdentityServiceBlockingStub blockingStub;

    @PostConstruct
    public void init() {
        // Build the channel immediately — no delay needed since @DependsOn("identityGrpcServer") guarantees order
        channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();
        blockingStub = IdentityServiceGrpc.newBlockingStub(channel);
        log.info("Identity gRPC Client đã khởi tạo thành công và kết nối đến port 50051");
    }

    /**
     * Connection test runs after full context is ready to avoid singleton lock contention
     * with beans like userTransactionManager that are initialized on the main thread.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void testConnectionOnReady() {
        testConnection();
    }

    public void testConnection() {
        int maxRetries = 5;
        int retryDelay = 2000; // 2 seconds
        
        for (int i = 0; i < maxRetries; i++) {
            try {
                log.info("=== Testing Identity gRPC Service (attempt {}/{}) ===", i + 1, maxRetries);
                
                // Test validateUser với user ID không tồn tại
                ValidateUserRequest validateRequest = ValidateUserRequest.newBuilder()
                        .setUserId("999")
                        .build();
                
                ValidateUserResponse validateResponse = blockingStub.validateUser(validateRequest);
                log.info("Validate User Response: valid={}, isActive={}, message={}", 
                        validateResponse.getValid(), 
                        validateResponse.getIsActive(), 
                        validateResponse.getMessage());
                
                // Test isUserActive
                IsUserActiveRequest activeRequest = IsUserActiveRequest.newBuilder()
                        .setUserId("999")
                        .build();
                
                IsUserActiveResponse activeResponse = blockingStub.isUserActive(activeRequest);
                log.info("Is User Active Response: userId={}, isActive={}, errorMessage={}", 
                        activeResponse.getUserId(), 
                        activeResponse.getIsActive(), 
                        activeResponse.getErrorMessage());
                
                // If we get here, connection is successful
                log.info("✅ Identity gRPC Service connection test PASSED!");
                return;
                
            } catch (Exception e) {
                log.warn("❌ Identity gRPC connection test attempt {} failed: {}", i + 1, e.getMessage());
                if (i < maxRetries - 1) {
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        log.error("🚨 Failed to connect to Identity gRPC Service after {} attempts", maxRetries);
    }

    public ValidateTokenResponse validateToken(String token) {
        try {
            ValidateTokenRequest request = ValidateTokenRequest.newBuilder()
                    .setToken(token)
                    .build();
            return blockingStub.validateToken(request);
        } catch (Exception e) {
            log.error("Lỗi khi validate token qua gRPC", e);
            return null;
        }
    }

    public GetUserResponse getUserProfile(String userId) {
        try {
            GetUserRequest request = GetUserRequest.newBuilder()
                    .setUserId(userId)
                    .build();
            return blockingStub.getUserProfile(request);
        } catch (Exception e) {
            log.error("Lỗi khi lấy user profile qua gRPC", e);
            return null;
        }
    }

    public ValidateUserResponse validateUser(String userId) {
        try {
            ValidateUserRequest request = ValidateUserRequest.newBuilder()
                    .setUserId(userId)
                    .build();
            return blockingStub.validateUser(request);
        } catch (Exception e) {
            log.error("Lỗi khi validate user qua gRPC", e);
            return null;
        }
    }

    public GetUserRoleResponse getUserRole(String userId) {
        try {
            GetUserRoleRequest request = GetUserRoleRequest.newBuilder()
                    .setUserId(userId)
                    .build();
            return blockingStub.getUserRole(request);
        } catch (Exception e) {
            log.error("Lỗi khi lấy user role qua gRPC", e);
            return null;
        }
    }

    public IsUserActiveResponse isUserActive(String userId) {
        try {
            IsUserActiveRequest request = IsUserActiveRequest.newBuilder()
                    .setUserId(userId)
                    .build();
            return blockingStub.isUserActive(request);
        } catch (Exception e) {
            log.error("Lỗi khi kiểm tra user active qua gRPC", e);
            return null;
        }
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null) {
            try {
                channel.shutdown().awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
                log.info("Identity gRPC Client đã shutdown");
            } catch (InterruptedException e) {
                channel.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
