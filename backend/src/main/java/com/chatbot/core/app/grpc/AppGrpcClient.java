package com.chatbot.core.app.grpc;

import com.chatbot.core.app.grpc.*;
import com.chatbot.core.grpc.resilience.GrpcResilienceConfig;
import com.chatbot.core.tenant.exception.GrpcIntegrationException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.springframework.context.annotation.DependsOn;

@Service
@DependsOn("appGrpcServer")
@Slf4j
public class AppGrpcClient {
    
    @Value("${app.grpc.server.host:localhost}")
    private String grpcHost;
    
    @Value("${app.grpc.server.port:50054}")
    private int grpcPort;
    
    @Value("${grpc.security.tls.enabled:false}")
    private boolean tlsEnabled;

    @Value("${grpc.client.timeout:30}")
    private int timeoutSeconds;

    private ManagedChannel channel;
    private AppServiceGrpc.AppServiceBlockingStub blockingStub;
    private AppServiceGrpc.AppServiceStub asyncStub;

    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public AppGrpcClient(CircuitBreaker appGrpcCircuitBreaker, Retry appGrpcRetry) {
        this.circuitBreaker = appGrpcCircuitBreaker;
        this.retry = appGrpcRetry;
    }

    @PostConstruct
    public void init() {
        try {
            ManagedChannelBuilder<?> builder = ManagedChannelBuilder.forAddress(grpcHost, grpcPort)
                .keepAliveTime(30, TimeUnit.SECONDS)
                .keepAliveTimeout(5, TimeUnit.SECONDS)
                .keepAliveWithoutCalls(true);

            if (tlsEnabled) {
                builder.useTransportSecurity();
            } else {
                builder.usePlaintext();
            }

            channel = builder.build();
            blockingStub = AppServiceGrpc.newBlockingStub(channel);
            asyncStub = AppServiceGrpc.newStub(channel);

            log.info("App gRPC client initialized: {}:{}", grpcHost, grpcPort);

            // Test connection
            testConnection();

        } catch (Exception e) {
            log.error("Failed to initialize App gRPC client: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void shutdown() {
        try {
            if (channel != null && !channel.isShutdown()) {
                channel.shutdown()
                        .awaitTermination(timeoutSeconds, TimeUnit.SECONDS);
                log.info("App gRPC client shutdown completed");
            }
        } catch (InterruptedException e) {
            log.warn("App gRPC client shutdown interrupted");
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Test connection to gRPC server with actual RPC call
     */
    private void testConnection() {
        try {
            // Use a lightweight GetApp request to test connection
            GetAppRequest request = GetAppRequest.newBuilder()
                    .setAppId(999L) // Use numeric ID for health check
                    .build();

            GetAppResponse response = blockingStub.withDeadlineAfter(5, TimeUnit.SECONDS)
                    .getApp(request);

            log.info("✅ App gRPC connection test PASSED - Server is responsive");

        } catch (io.grpc.StatusRuntimeException e) {
            if (e.getStatus().getCode() == io.grpc.Status.Code.NOT_FOUND) {
                // Expected - health-check app doesn't exist, but connection works
                log.info("✅ App gRPC connection test PASSED - Server is responsive (expected NOT_FOUND error)");
            } else {
                log.warn("❌ App gRPC connection test failed: {} - {}", e.getStatus().getCode(), e.getStatus().getDescription());
            }
        } catch (Exception e) {
            log.error("❌ App gRPC connection test failed: {}", e.getMessage());
        }
    }
    
    public AppServiceGrpc.AppServiceBlockingStub getBlockingStub() {
        if (blockingStub == null) {
            init();
        }
        return blockingStub;
    }

    public AppServiceGrpc.AppServiceStub getAsyncStub() {
        if (asyncStub == null) {
            init();
        }
        return asyncStub;
    }

    /**
     * Get the managed channel for use by other services
     */
    public ManagedChannel getChannel() {
        return channel;
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
        log.info("Reconnecting to App gRPC server...");
        shutdown();
        init();
    }

    // Convenience methods for common operations with circuit breaker and retry
    public RegisterAppResponse registerApp(RegisterAppRequest request) {
        Supplier<RegisterAppResponse> supplier = CircuitBreaker.decorateSupplier(circuitBreaker,
            Retry.decorateSupplier(retry, () -> {
                try {
                    return getBlockingStub().registerApp(request);
                } catch (io.grpc.StatusRuntimeException e) {
                    log.error("gRPC registerApp failed: {} - {}", e.getStatus().getCode(), e.getStatus().getDescription());
                    throw new GrpcIntegrationException("Failed to register app: " + e.getStatus().getDescription(), e);
                }
            }));

        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Circuit breaker/retry failed for registerApp: {}", e.getMessage());
            throw new GrpcIntegrationException("Failed to register app after retry: " + e.getMessage(), e);
        }
    }

    public GetAppResponse getApp(GetAppRequest request) {
        Supplier<GetAppResponse> supplier = CircuitBreaker.decorateSupplier(circuitBreaker,
            Retry.decorateSupplier(retry, () -> {
                try {
                    return getBlockingStub().getApp(request);
                } catch (io.grpc.StatusRuntimeException e) {
                    log.error("gRPC getApp failed for appId {}: {} - {}", request.getAppId(), e.getStatus().getCode(), e.getStatus().getDescription());
                    throw new GrpcIntegrationException("Failed to get app: " + e.getStatus().getDescription(), e);
                }
            }));

        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Circuit breaker/retry failed for getApp: {}", e.getMessage());
            throw new GrpcIntegrationException("Failed to get app after retry: " + e.getMessage(), e);
        }
    }

    public SearchAppsResponse searchApps(SearchAppsRequest request) {
        Supplier<SearchAppsResponse> supplier = CircuitBreaker.decorateSupplier(circuitBreaker,
            Retry.decorateSupplier(retry, () -> {
                try {
                    return getBlockingStub().searchApps(request);
                } catch (io.grpc.StatusRuntimeException e) {
                    log.error("gRPC searchApps failed: {} - {}", e.getStatus().getCode(), e.getStatus().getDescription());
                    throw new GrpcIntegrationException("Failed to search apps: " + e.getStatus().getDescription(), e);
                }
            }));

        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Circuit breaker/retry failed for searchApps: {}", e.getMessage());
            throw new GrpcIntegrationException("Failed to search apps after retry: " + e.getMessage(), e);
        }
    }

    public SubscribeAppResponse subscribeToApp(SubscribeAppRequest request) {
        Supplier<SubscribeAppResponse> supplier = CircuitBreaker.decorateSupplier(circuitBreaker,
            Retry.decorateSupplier(retry, () -> {
                try {
                    return getBlockingStub().subscribeToApp(request);
                } catch (io.grpc.StatusRuntimeException e) {
                    log.error("gRPC subscribeToApp failed: {} - {}", e.getStatus().getCode(), e.getStatus().getDescription());
                    throw new GrpcIntegrationException("Failed to subscribe to app: " + e.getStatus().getDescription(), e);
                }
            }));

        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Circuit breaker/retry failed for subscribeToApp: {}", e.getMessage());
            throw new GrpcIntegrationException("Failed to subscribe to app after retry: " + e.getMessage(), e);
        }
    }

    public GetSubscriptionResponse getSubscription(GetSubscriptionRequest request) {
        Supplier<GetSubscriptionResponse> supplier = CircuitBreaker.decorateSupplier(circuitBreaker,
            Retry.decorateSupplier(retry, () -> {
                try {
                    return getBlockingStub().getSubscription(request);
                } catch (io.grpc.StatusRuntimeException e) {
                    log.error("gRPC getSubscription failed: {} - {}", e.getStatus().getCode(), e.getStatus().getDescription());
                    throw new GrpcIntegrationException("Failed to get subscription: " + e.getStatus().getDescription(), e);
                }
            }));

        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Circuit breaker/retry failed for getSubscription: {}", e.getMessage());
            throw new GrpcIntegrationException("Failed to get subscription after retry: " + e.getMessage(), e);
        }
    }

    public EvaluateGuardResponse evaluateGuard(EvaluateGuardRequest request) {
        Supplier<EvaluateGuardResponse> supplier = CircuitBreaker.decorateSupplier(circuitBreaker,
            Retry.decorateSupplier(retry, () -> {
                try {
                    return getBlockingStub().evaluateGuard(request);
                } catch (io.grpc.StatusRuntimeException e) {
                    log.error("gRPC evaluateGuard failed: {} - {}", e.getStatus().getCode(), e.getStatus().getDescription());
                    throw new GrpcIntegrationException("Failed to evaluate guard: " + e.getStatus().getDescription(), e);
                }
            }));

        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Circuit breaker/retry failed for evaluateGuard: {}", e.getMessage());
            throw new GrpcIntegrationException("Failed to evaluate guard after retry: " + e.getMessage(), e);
        }
    }
}
