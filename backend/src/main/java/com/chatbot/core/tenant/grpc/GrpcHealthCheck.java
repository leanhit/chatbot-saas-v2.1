package com.chatbot.core.tenant.grpc;

import lombok.RequiredArgsConstructor;
import com.chatbot.core.tenant.grpc.TenantServiceProto.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public @RequiredArgsConstructor
class GrpcHealthCheck {


    private final TenantServiceGrpcImpl grpcService;

    @Value("${tenant.grpc.server.port:50057}")
    private int grpcPort;

    @PostConstruct
    public void performHealthCheck() {
        ManagedChannel channel = null;
        try {
            log.info("=== Bắt đầu Health Check cho gRPC Tenant Service ===");
            
            // Tạo channel để test
            channel = ManagedChannelBuilder.forAddress("localhost", grpcPort)
                    .usePlaintext()
                    .build();
            
            // Add authorization metadata for health check
            Metadata headers = new Metadata();
            Metadata.Key<String> authorizationKey = Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);
            // Use special health check token that bypasses JWT validation
            headers.put(authorizationKey, "Bearer health-check-token");
            
            TenantServiceGrpc.TenantServiceBlockingStub blockingStub = TenantServiceGrpc.newBlockingStub(channel)
                    .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));
            
            // Test validateTenant với tenant key không tồn tại
            ValidateTenantRequest request = ValidateTenantRequest.newBuilder()
                    .setTenantKey("health-check-test")
                    .build();
            
            ValidateTenantResponse response = blockingStub.validateTenant(request);
            
            log.info("✅ gRPC Health Check PASSED!");
            log.info("   - Response valid: {}", response.getValid());
            log.info("   - Response status: {}", response.getStatus());
            log.info("   - Response message: {}", response.getMessage());
            log.info("   - gRPC Server đang chạy trên port {}", grpcPort);
            
        } catch (Exception e) {
            log.error("❌ gRPC Health Check FAILED: {}", e.getMessage(), e);
        } finally {
            if (channel != null) {
                try {
                    channel.shutdown();
                    channel.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    log.warn("Failed to shutdown channel properly", e);
                }
            }
        }
    }
}
