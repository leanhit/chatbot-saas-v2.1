package com.chatbot.core.tenant.grpc;

import com.chatbot.core.grpc.resilience.GrpcResilienceConfig;
import com.chatbot.core.tenant.exception.GrpcIntegrationException;
import com.chatbot.core.tenant.grpc.TenantServiceProto.*;
import com.chatbot.core.tenant.grpc.TenantServiceGrpc;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import java.util.function.Supplier;

@Component
@DependsOn("grpcServerConfig")
@Slf4j
public class TenantGrpcClient {

    private ManagedChannel channel;
    private TenantServiceGrpc.TenantServiceBlockingStub blockingStub;

    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public TenantGrpcClient(CircuitBreaker tenantGrpcCircuitBreaker, Retry tenantGrpcRetry) {
        this.circuitBreaker = tenantGrpcCircuitBreaker;
        this.retry = tenantGrpcRetry;
    }

    @Value("${grpc.server.identity.host:localhost}")
    private String grpcHost;

    @Value("${tenant.grpc.server.port:50057}")
    private int grpcPort;

    @Value("${grpc.security.tls.enabled:false}")
    private boolean tlsEnabled;

    @PostConstruct
    public void init() {
        try {
            // Reduced delay since @DependsOn ensures server starts first
            Thread.sleep(1000);
            
            // Tạo channel kết nối đến gRPC server
            ManagedChannelBuilder<?> builder = ManagedChannelBuilder.forAddress(grpcHost, grpcPort);
            if (tlsEnabled) {
                builder.useTransportSecurity();
            } else {
                builder.usePlaintext();
            }
            channel = builder.build();
            
            blockingStub = TenantServiceGrpc.newBlockingStub(channel);
            
            log.info("gRPC Client đã khởi tạo thành công và kết nối đến {}:{}", grpcHost, grpcPort);
            
            // Test kết nối
            testConnection();
            
        } catch (Exception e) {
            log.error("Lỗi khi khởi tạo gRPC client", e);
        }
    }

    public void testConnection() {
        int maxRetries = 5;
        int retryDelay = 2000; // 2 seconds
        
        // Add health check token for connection test
        Metadata headers = new Metadata();
        Metadata.Key<String> authorizationKey = Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);
        headers.put(authorizationKey, "Bearer health-check-token");
        
        TenantServiceGrpc.TenantServiceBlockingStub testStub = blockingStub.withInterceptors(
                MetadataUtils.newAttachHeadersInterceptor(headers));
        
        for (int i = 0; i < maxRetries; i++) {
            try {
                log.info("=== Testing gRPC Tenant Service (attempt {}/{}) ===", i + 1, maxRetries);
                
                // Test validateTenant
                ValidateTenantRequest validateRequest = ValidateTenantRequest.newBuilder()
                        .setTenantKey("test-tenant-key")
                        .build();
                
                ValidateTenantResponse validateResponse = testStub.validateTenant(validateRequest);
                log.info("Validate Tenant Response: valid={}, status={}, message={}", 
                        validateResponse.getValid(), 
                        validateResponse.getStatus(), 
                        validateResponse.getMessage());
                
                // Test checkTenantExists
                CheckTenantExistsRequest existsRequest = CheckTenantExistsRequest.newBuilder()
                        .setTenantKey("test-tenant-key")
                        .build();
                
                CheckTenantExistsResponse existsResponse = testStub.checkTenantExists(existsRequest);
                log.info("Check Tenant Exists Response: exists={}, tenantKey={}", 
                        existsResponse.getExists(), 
                        existsResponse.getTenantKey());
                
                // If we get here, connection is successful
                log.info("✅ gRPC Tenant Service connection test PASSED!");
                return;
                
            } catch (Exception e) {
                log.warn("❌ gRPC connection test attempt {} failed: {}", i + 1, e.getMessage());
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
        
        log.error("🚨 Failed to connect to gRPC Tenant Service after {} attempts", maxRetries);
    }

    public ValidateTenantResponse validateTenant(String tenantKey) {
        Supplier<ValidateTenantResponse> supplier = CircuitBreaker.decorateSupplier(circuitBreaker,
            Retry.decorateSupplier(retry, () -> {
                try {
                    ValidateTenantRequest request = ValidateTenantRequest.newBuilder()
                            .setTenantKey(tenantKey)
                            .build();
                    return blockingStub.validateTenant(request);
                } catch (io.grpc.StatusRuntimeException e) {
                    log.error("gRPC validateTenant failed for tenantKey {}: {} - {}", tenantKey, e.getStatus().getCode(), e.getStatus().getDescription());
                    throw new GrpcIntegrationException("Failed to validate tenant: " + e.getStatus().getDescription(), e);
                }
            }));

        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Circuit breaker/retry failed for validateTenant: {}", e.getMessage());
            throw new GrpcIntegrationException("Failed to validate tenant after retry: " + e.getMessage(), e);
        }
    }

    public CheckTenantExistsResponse checkTenantExists(String tenantKey) {
        Supplier<CheckTenantExistsResponse> supplier = CircuitBreaker.decorateSupplier(circuitBreaker,
            Retry.decorateSupplier(retry, () -> {
                try {
                    CheckTenantExistsRequest request = CheckTenantExistsRequest.newBuilder()
                            .setTenantKey(tenantKey)
                            .build();
                    return blockingStub.checkTenantExists(request);
                } catch (io.grpc.StatusRuntimeException e) {
                    log.error("gRPC checkTenantExists failed for tenantKey {}: {} - {}", tenantKey, e.getStatus().getCode(), e.getStatus().getDescription());
                    throw new GrpcIntegrationException("Failed to check tenant exists: " + e.getStatus().getDescription(), e);
                }
            }));

        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Circuit breaker/retry failed for checkTenantExists: {}", e.getMessage());
            throw new GrpcIntegrationException("Failed to check tenant exists after retry: " + e.getMessage(), e);
        }
    }

    public TenantDetailResponse getTenant(String tenantKey) {
        Supplier<TenantDetailResponse> supplier = CircuitBreaker.decorateSupplier(circuitBreaker,
            Retry.decorateSupplier(retry, () -> {
                try {
                    GetTenantRequest request = GetTenantRequest.newBuilder()
                            .setTenantKey(tenantKey)
                            .build();
                    return blockingStub.getTenant(request);
                } catch (io.grpc.StatusRuntimeException e) {
                    log.error("gRPC getTenant failed for tenantKey {}: {} - {}", tenantKey, e.getStatus().getCode(), e.getStatus().getDescription());
                    throw new GrpcIntegrationException("Failed to get tenant: " + e.getStatus().getDescription(), e);
                }
            }));

        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Circuit breaker/retry failed for getTenant: {}", e.getMessage());
            throw new GrpcIntegrationException("Failed to get tenant after retry: " + e.getMessage(), e);
        }
    }

    public ListTenantsResponse listTenants(int page, int size) {
        Supplier<ListTenantsResponse> supplier = CircuitBreaker.decorateSupplier(circuitBreaker,
            Retry.decorateSupplier(retry, () -> {
                try {
                    ListTenantsRequest request = ListTenantsRequest.newBuilder()
                            .setPage(page)
                            .setSize(size)
                            .build();
                    return blockingStub.listTenants(request);
                } catch (io.grpc.StatusRuntimeException e) {
                    log.error("gRPC listTenants failed: {} - {}", e.getStatus().getCode(), e.getStatus().getDescription());
                    throw new GrpcIntegrationException("Failed to list tenants: " + e.getStatus().getDescription(), e);
                }
            }));

        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Circuit breaker/retry failed for listTenants: {}", e.getMessage());
            throw new GrpcIntegrationException("Failed to list tenants after retry: " + e.getMessage(), e);
        }
    }

    public TenantResponse createTenant(CreateTenantRequest request) {
        Supplier<TenantResponse> supplier = CircuitBreaker.decorateSupplier(circuitBreaker,
            Retry.decorateSupplier(retry, () -> {
                try {
                    return blockingStub.createTenant(request);
                } catch (io.grpc.StatusRuntimeException e) {
                    log.error("gRPC createTenant failed: {} - {}", e.getStatus().getCode(), e.getStatus().getDescription());
                    throw new GrpcIntegrationException("Failed to create tenant: " + e.getStatus().getDescription(), e);
                }
            }));

        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Circuit breaker/retry failed for createTenant: {}", e.getMessage());
            throw new GrpcIntegrationException("Failed to create tenant after retry: " + e.getMessage(), e);
        }
    }

    public SearchTenantsResponse searchTenants(SearchTenantsRequest request) {
        Supplier<SearchTenantsResponse> supplier = CircuitBreaker.decorateSupplier(circuitBreaker,
            Retry.decorateSupplier(retry, () -> {
                try {
                    log.info("gRPC Client: Bắt đầu search tenants - query: {}, page: {}, size: {}", 
                            request.getQuery(), request.getPage(), request.getSize());
                    SearchTenantsResponse response = blockingStub.searchTenants(request);
                    log.info("gRPC Client: Search thành công, trả về {} tenants", response.getTenantsCount());
                    return response;
                } catch (io.grpc.StatusRuntimeException e) {
                    log.error("gRPC searchTenants failed: {} - {}", e.getStatus().getCode(), e.getStatus().getDescription());
                    throw new GrpcIntegrationException("Failed to search tenants: " + e.getStatus().getDescription(), e);
                }
            }));

        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Circuit breaker/retry failed for searchTenants: {}", e.getMessage());
            throw new GrpcIntegrationException("Failed to search tenants after retry: " + e.getMessage(), e);
        }
    }

    public TenantResponse suspendTenant(SuspendTenantRequest request) {
        Supplier<TenantResponse> supplier = CircuitBreaker.decorateSupplier(circuitBreaker,
            Retry.decorateSupplier(retry, () -> {
                try {
                    return blockingStub.suspendTenant(request);
                } catch (io.grpc.StatusRuntimeException e) {
                    log.error("gRPC suspendTenant failed: {} - {}", e.getStatus().getCode(), e.getStatus().getDescription());
                    throw new GrpcIntegrationException("Failed to suspend tenant: " + e.getStatus().getDescription(), e);
                }
            }));

        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Circuit breaker/retry failed for suspendTenant: {}", e.getMessage());
            throw new GrpcIntegrationException("Failed to suspend tenant after retry: " + e.getMessage(), e);
        }
    }

    public TenantResponse activateTenant(ActivateTenantRequest request) {
        Supplier<TenantResponse> supplier = CircuitBreaker.decorateSupplier(circuitBreaker,
            Retry.decorateSupplier(retry, () -> {
                try {
                    return blockingStub.activateTenant(request);
                } catch (io.grpc.StatusRuntimeException e) {
                    log.error("gRPC activateTenant failed: {} - {}", e.getStatus().getCode(), e.getStatus().getDescription());
                    throw new GrpcIntegrationException("Failed to activate tenant: " + e.getStatus().getDescription(), e);
                }
            }));

        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Circuit breaker/retry failed for activateTenant: {}", e.getMessage());
            throw new GrpcIntegrationException("Failed to activate tenant after retry: " + e.getMessage(), e);
        }
    }

    public TenantStatusResponse getTenantStatus(GetTenantStatusRequest request) {
        Supplier<TenantStatusResponse> supplier = CircuitBreaker.decorateSupplier(circuitBreaker,
            Retry.decorateSupplier(retry, () -> {
                try {
                    return blockingStub.getTenantStatus(request);
                } catch (io.grpc.StatusRuntimeException e) {
                    log.error("gRPC getTenantStatus failed: {} - {}", e.getStatus().getCode(), e.getStatus().getDescription());
                    throw new GrpcIntegrationException("Failed to get tenant status: " + e.getStatus().getDescription(), e);
                }
            }));

        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Circuit breaker/retry failed for getTenantStatus: {}", e.getMessage());
            throw new GrpcIntegrationException("Failed to get tenant status after retry: " + e.getMessage(), e);
        }
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null) {
            try {
                channel.shutdown().awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
                log.info("gRPC Client đã shutdown");
            } catch (InterruptedException e) {
                channel.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
