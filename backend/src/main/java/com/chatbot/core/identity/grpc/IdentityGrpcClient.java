package com.chatbot.core.identity.grpc;

import com.chatbot.core.grpc.resilience.GrpcResilienceConfig;
import com.chatbot.core.identity.grpc.IdentityServiceOuterClass.*;
import com.chatbot.core.identity.grpc.IdentityServiceGrpc;
import com.chatbot.core.tenant.exception.GrpcIntegrationException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
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
import java.util.function.Supplier;

@Component
@DependsOn("identityGrpcServer")
@Slf4j
public class IdentityGrpcClient {

    @org.springframework.beans.factory.annotation.Value("${grpc.security.tls.enabled:false}")
    private boolean tlsEnabled;

    private ManagedChannel channel;
    private IdentityServiceGrpc.IdentityServiceBlockingStub blockingStub;

    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public IdentityGrpcClient(CircuitBreaker identityGrpcCircuitBreaker, Retry identityGrpcRetry) {
        this.circuitBreaker = identityGrpcCircuitBreaker;
        this.retry = identityGrpcRetry;
    }

    @PostConstruct
    public void init() {
        // Build the channel immediately — no delay needed since @DependsOn("identityGrpcServer") guarantees order
        ManagedChannelBuilder<?> builder = ManagedChannelBuilder.forAddress("localhost", 50051);
        if (tlsEnabled) {
            builder.useTransportSecurity();
        } else {
            builder.usePlaintext();
        }
        channel = builder.build();
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
        Supplier<ValidateTokenResponse> supplier = CircuitBreaker.decorateSupplier(circuitBreaker,
            Retry.decorateSupplier(retry, () -> {
                try {
                    ValidateTokenRequest request = ValidateTokenRequest.newBuilder()
                            .setToken(token)
                            .build();
                    return blockingStub.validateToken(request);
                } catch (io.grpc.StatusRuntimeException e) {
                    log.error("gRPC validateToken failed: {} - {}", e.getStatus().getCode(), e.getStatus().getDescription());
                    throw new GrpcIntegrationException("Failed to validate token: " + e.getStatus().getDescription(), e);
                }
            }));

        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Circuit breaker/retry failed for validateToken: {}", e.getMessage());
            throw new GrpcIntegrationException("Failed to validate token after retry: " + e.getMessage(), e);
        }
    }

    public GetUserResponse getUserProfile(String userId) {
        Supplier<GetUserResponse> supplier = CircuitBreaker.decorateSupplier(circuitBreaker,
            Retry.decorateSupplier(retry, () -> {
                try {
                    GetUserRequest request = GetUserRequest.newBuilder()
                            .setUserId(userId)
                            .build();
                    return blockingStub.getUserProfile(request);
                } catch (io.grpc.StatusRuntimeException e) {
                    log.error("gRPC getUserProfile failed for userId {}: {} - {}", userId, e.getStatus().getCode(), e.getStatus().getDescription());
                    throw new GrpcIntegrationException("Failed to get user profile: " + e.getStatus().getDescription(), e);
                }
            }));

        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Circuit breaker/retry failed for getUserProfile: {}", e.getMessage());
            throw new GrpcIntegrationException("Failed to get user profile after retry: " + e.getMessage(), e);
        }
    }

    public ValidateUserResponse validateUser(String userId) {
        Supplier<ValidateUserResponse> supplier = CircuitBreaker.decorateSupplier(circuitBreaker,
            Retry.decorateSupplier(retry, () -> {
                try {
                    ValidateUserRequest request = ValidateUserRequest.newBuilder()
                            .setUserId(userId)
                            .build();
                    return blockingStub.validateUser(request);
                } catch (io.grpc.StatusRuntimeException e) {
                    log.error("gRPC validateUser failed for userId {}: {} - {}", userId, e.getStatus().getCode(), e.getStatus().getDescription());
                    throw new GrpcIntegrationException("Failed to validate user: " + e.getStatus().getDescription(), e);
                }
            }));

        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Circuit breaker/retry failed for validateUser: {}", e.getMessage());
            throw new GrpcIntegrationException("Failed to validate user after retry: " + e.getMessage(), e);
        }
    }

    public GetUserRoleResponse getUserRole(String userId) {
        Supplier<GetUserRoleResponse> supplier = CircuitBreaker.decorateSupplier(circuitBreaker,
            Retry.decorateSupplier(retry, () -> {
                try {
                    GetUserRoleRequest request = GetUserRoleRequest.newBuilder()
                            .setUserId(userId)
                            .build();
                    return blockingStub.getUserRole(request);
                } catch (io.grpc.StatusRuntimeException e) {
                    log.error("gRPC getUserRole failed for userId {}: {} - {}", userId, e.getStatus().getCode(), e.getStatus().getDescription());
                    throw new GrpcIntegrationException("Failed to get user role: " + e.getStatus().getDescription(), e);
                }
            }));

        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Circuit breaker/retry failed for getUserRole: {}", e.getMessage());
            throw new GrpcIntegrationException("Failed to get user role after retry: " + e.getMessage(), e);
        }
    }

    public IsUserActiveResponse isUserActive(String userId) {
        Supplier<IsUserActiveResponse> supplier = CircuitBreaker.decorateSupplier(circuitBreaker,
            Retry.decorateSupplier(retry, () -> {
                try {
                    IsUserActiveRequest request = IsUserActiveRequest.newBuilder()
                            .setUserId(userId)
                            .build();
                    return blockingStub.isUserActive(request);
                } catch (io.grpc.StatusRuntimeException e) {
                    log.error("gRPC isUserActive failed for userId {}: {} - {}", userId, e.getStatus().getCode(), e.getStatus().getDescription());
                    throw new GrpcIntegrationException("Failed to check user active status: " + e.getStatus().getDescription(), e);
                }
            }));

        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Circuit breaker/retry failed for isUserActive: {}", e.getMessage());
            throw new GrpcIntegrationException("Failed to check user active status after retry: " + e.getMessage(), e);
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
