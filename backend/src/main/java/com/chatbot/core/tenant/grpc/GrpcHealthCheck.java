package com.chatbot.core.tenant.grpc;

import com.chatbot.core.tenant.grpc.TenantServiceProto.*;
import com.chatbot.core.tenant.grpc.TenantServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public class GrpcHealthCheck {

    @Autowired
    private TenantServiceGrpcImpl grpcService;

    @PostConstruct
    public void performHealthCheck() {
        try {
            log.info("=== Bắt đầu Health Check cho gRPC Tenant Service ===");
            
            // Tạo channel để test
            ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50053)
                    .usePlaintext()
                    .build();
            
            TenantServiceGrpc.TenantServiceBlockingStub blockingStub = TenantServiceGrpc.newBlockingStub(channel);
            
            // Test validateTenant với tenant key không tồn tại
            ValidateTenantRequest request = ValidateTenantRequest.newBuilder()
                    .setTenantKey("health-check-test")
                    .build();
            
            ValidateTenantResponse response = blockingStub.validateTenant(request);
            
            log.info("✅ gRPC Health Check PASSED!");
            log.info("   - Response valid: {}", response.getValid());
            log.info("   - Response status: {}", response.getStatus());
            log.info("   - Response message: {}", response.getMessage());
            log.info("   - gRPC Server đang chạy trên port 50053");
            
            channel.shutdown();
            
        } catch (Exception e) {
            log.error("❌ gRPC Health Check FAILED: {}", e.getMessage(), e);
        }
    }
}
