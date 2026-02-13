package com.chatbot.core.tenant.grpc;

import com.chatbot.core.tenant.grpc.TenantServiceProto.*;
import com.chatbot.core.tenant.grpc.TenantServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
@DependsOn("grpcServerConfig")
@Slf4j
public class TenantGrpcClient {

    private ManagedChannel channel;
    private TenantServiceGrpc.TenantServiceBlockingStub blockingStub;

    @PostConstruct
    public void init() {
        try {
            // Reduced delay since @DependsOn ensures server starts first
            Thread.sleep(1000);
            
            // Tạo channel kết nối đến gRPC server
            channel = ManagedChannelBuilder.forAddress("localhost", 50053)
                    .usePlaintext()
                    .build();
            
            blockingStub = TenantServiceGrpc.newBlockingStub(channel);
            
            log.info("gRPC Client đã khởi tạo thành công và kết nối đến port 50053");
            
            // Test kết nối
            testConnection();
            
        } catch (Exception e) {
            log.error("Lỗi khi khởi tạo gRPC client", e);
        }
    }

    public void testConnection() {
        int maxRetries = 5;
        int retryDelay = 2000; // 2 seconds
        
        for (int i = 0; i < maxRetries; i++) {
            try {
                log.info("=== Testing gRPC Tenant Service (attempt {}/{}) ===", i + 1, maxRetries);
                
                // Test validateTenant
                ValidateTenantRequest validateRequest = ValidateTenantRequest.newBuilder()
                        .setTenantKey("test-tenant-key")
                        .build();
                
                ValidateTenantResponse validateResponse = blockingStub.validateTenant(validateRequest);
                log.info("Validate Tenant Response: valid={}, status={}, message={}", 
                        validateResponse.getValid(), 
                        validateResponse.getStatus(), 
                        validateResponse.getMessage());
                
                // Test checkTenantExists
                CheckTenantExistsRequest existsRequest = CheckTenantExistsRequest.newBuilder()
                        .setTenantKey("test-tenant-key")
                        .build();
                
                CheckTenantExistsResponse existsResponse = blockingStub.checkTenantExists(existsRequest);
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
        try {
            ValidateTenantRequest request = ValidateTenantRequest.newBuilder()
                    .setTenantKey(tenantKey)
                    .build();
            return blockingStub.validateTenant(request);
        } catch (Exception e) {
            log.error("Lỗi khi validate tenant qua gRPC", e);
            return null;
        }
    }

    public CheckTenantExistsResponse checkTenantExists(String tenantKey) {
        try {
            CheckTenantExistsRequest request = CheckTenantExistsRequest.newBuilder()
                    .setTenantKey(tenantKey)
                    .build();
            return blockingStub.checkTenantExists(request);
        } catch (Exception e) {
            log.error("Lỗi khi kiểm tra tenant tồn tại qua gRPC", e);
            return null;
        }
    }

    public TenantDetailResponse getTenant(String tenantKey) {
        try {
            GetTenantRequest request = GetTenantRequest.newBuilder()
                    .setTenantKey(tenantKey)
                    .build();
            return blockingStub.getTenant(request);
        } catch (Exception e) {
            log.error("Lỗi khi lấy tenant qua gRPC", e);
            return null;
        }
    }

    public ListTenantsResponse listTenants(int page, int size) {
        try {
            ListTenantsRequest request = ListTenantsRequest.newBuilder()
                    .setPage(page)
                    .setSize(size)
                    .build();
            return blockingStub.listTenants(request);
        } catch (Exception e) {
            log.error("Lỗi khi list tenants qua gRPC", e);
            return null;
        }
    }

    public TenantResponse createTenant(CreateTenantRequest request) {
        try {
            return blockingStub.createTenant(request);
        } catch (Exception e) {
            log.error("Lỗi khi create tenant qua gRPC", e);
            return null;
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
