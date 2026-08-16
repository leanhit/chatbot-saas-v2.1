package com.chatbot.core.user.grpc;

import com.chatbot.core.grpc.resilience.GrpcResilienceConfig;
import com.chatbot.core.tenant.exception.GrpcIntegrationException;
import com.chatbot.core.user.grpc.UserServiceProto.*;
import com.chatbot.core.user.grpc.UserServiceGrpc;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * User gRPC Client - For calling User Hub from other hubs
 */
@Component
@DependsOn("userGrpcServer")
@Slf4j
public class UserGrpcClient {

    @Value("${user.grpc.server.host:localhost}")
    private String host;

    @Value("${user.grpc.server.port:50052}")
    private int port;

    @Value("${grpc.security.tls.enabled:false}")
    private boolean tlsEnabled;

    @Value("${grpc.client.timeout:30}")
    private int timeoutSeconds;

    private ManagedChannel channel;
    private UserServiceGrpc.UserServiceBlockingStub blockingStub;

    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public UserGrpcClient(CircuitBreaker userGrpcCircuitBreaker, Retry userGrpcRetry) {
        this.circuitBreaker = userGrpcCircuitBreaker;
        this.retry = userGrpcRetry;
    }

    @PostConstruct
    public void init() {
        try {
            ManagedChannelBuilder<?> builder = ManagedChannelBuilder.forAddress(host, port)
                    .keepAliveTime(30, TimeUnit.SECONDS)
                    .keepAliveTimeout(5, TimeUnit.SECONDS)
                    .keepAliveWithoutCalls(true);

            if (tlsEnabled) {
                builder.useTransportSecurity();
            } else {
                builder.usePlaintext();
            }

            channel = builder.build();
            blockingStub = UserServiceGrpc.newBlockingStub(channel);

            log.info("User gRPC client initialized: {}:{}", host, port);

            // Test connection
            testConnection();

        } catch (Exception e) {
            log.error("Failed to initialize User gRPC client: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (channel != null && !channel.isShutdown()) {
                channel.shutdown()
                        .awaitTermination(timeoutSeconds, TimeUnit.SECONDS);
                log.info("User gRPC client shutdown completed");
            }
        } catch (InterruptedException e) {
            log.warn("User gRPC client shutdown interrupted");
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Test connection to gRPC server with actual RPC call
     */
    private void testConnection() {
        try {
            // Use a lightweight RPC call to test connection
            ValidateUserRequest request = ValidateUserRequest.newBuilder()
                    .setUserId("health-check")
                    .build();

            ValidateUserResponse response = blockingStub.withDeadlineAfter(5, TimeUnit.SECONDS)
                    .validateUser(request);

            log.info("✅ User gRPC connection test PASSED - Server is responsive");

        } catch (io.grpc.StatusRuntimeException e) {
            if (e.getStatus().getCode() == io.grpc.Status.Code.INVALID_ARGUMENT) {
                // Expected - health-check user doesn't exist, but connection works
                log.info("✅ User gRPC connection test PASSED - Server is responsive (expected validation error)");
            } else {
                log.warn("❌ User gRPC connection test failed: {} - {}", e.getStatus().getCode(), e.getStatus().getDescription());
            }
        } catch (Exception e) {
            log.error("❌ User gRPC connection test failed: {}", e.getMessage());
        }
    }

    /**
     * Validate user exists and is active with circuit breaker and retry
     */
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

    /**
     * Get user profile information with circuit breaker and retry
     */
    public GetUserProfileResponse getUserProfile(String userId) {
        Supplier<GetUserProfileResponse> supplier = CircuitBreaker.decorateSupplier(circuitBreaker,
            Retry.decorateSupplier(retry, () -> {
                try {
                    GetUserProfileRequest request = GetUserProfileRequest.newBuilder()
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

    /**
     * Get user basic information with circuit breaker and retry
     */
    public GetUserBasicInfoResponse getUserBasicInfo(String userId) {
        Supplier<GetUserBasicInfoResponse> supplier = CircuitBreaker.decorateSupplier(circuitBreaker,
            Retry.decorateSupplier(retry, () -> {
                try {
                    GetUserBasicInfoRequest request = GetUserBasicInfoRequest.newBuilder()
                            .setUserId(userId)
                            .build();
                    return blockingStub.getUserBasicInfo(request);
                } catch (io.grpc.StatusRuntimeException e) {
                    log.error("gRPC getUserBasicInfo failed for userId {}: {} - {}", userId, e.getStatus().getCode(), e.getStatus().getDescription());
                    throw new GrpcIntegrationException("Failed to get user basic info: " + e.getStatus().getDescription(), e);
                }
            }));

        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Circuit breaker/retry failed for getUserBasicInfo: {}", e.getMessage());
            throw new GrpcIntegrationException("Failed to get user basic info after retry: " + e.getMessage(), e);
        }
    }

    /**
     * Check if user is active with circuit breaker and retry
     */
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

    /**
     * Get the managed channel for use by other services
     */
    public ManagedChannel getChannel() {
        return channel;
    }

    /**
     * Get the blocking stub for direct use if needed
     */
    public UserServiceGrpc.UserServiceBlockingStub getBlockingStub() {
        return blockingStub;
    }

    /**
     * Check if client is ready
     */
    public boolean isReady() {
        return channel != null && !channel.isShutdown() && !channel.isTerminated();
    }

    /**
     * Get connection status
     */
    public String getConnectionStatus() {
        if (channel == null) {
            return "NOT_INITIALIZED";
        }
        if (channel.isShutdown()) {
            return "SHUTDOWN";
        }
        if (channel.isTerminated()) {
            return "TERMINATED";
        }
        return "CONNECTED";
    }

    /**
     * Reconnect to gRPC server
     */
    public void reconnect() {
        log.info("Reconnecting to User gRPC server...");
        cleanup();
        init();
    }
}
