package com.chatbot.core.identity.grpc;

import com.chatbot.core.identity.grpc.IdentityServiceOuterClass.*;
import com.chatbot.core.identity.grpc.IdentityServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import java.util.concurrent.TimeUnit;

@Component
@DependsOn("identityGrpcServer")
@Slf4j
public class IdentityGrpcClient {

    private ManagedChannel channel;
    private IdentityServiceGrpc.IdentityServiceBlockingStub blockingStub;

    @PostConstruct
    public void init() {
        try {
            // Reduced delay since @DependsOn ensures server starts first
            Thread.sleep(1000);
            
            // Tạo channel kết nối đến Identity gRPC server
            channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                    .usePlaintext()
                    .build();
            
            blockingStub = IdentityServiceGrpc.newBlockingStub(channel);
            
            log.info("Identity gRPC Client đã khởi tạo thành công và kết nối đến port 50051");
            
            // Test kết nối
            testConnection();
            
        } catch (Exception e) {
            log.error("Lỗi khi khởi tạo Identity gRPC client", e);
        }
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
