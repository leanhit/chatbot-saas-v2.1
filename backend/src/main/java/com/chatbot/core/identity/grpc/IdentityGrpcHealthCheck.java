package com.chatbot.core.identity.grpc;

import com.chatbot.core.identity.grpc.IdentityServiceOuterClass.*;
import com.chatbot.core.identity.grpc.IdentityServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public class IdentityGrpcHealthCheck {

    @Autowired
    private IdentityServiceGrpcImpl grpcService;

    @PostConstruct
    public void performHealthCheck() {
        try {
            log.info("=== Bắt đầu Health Check cho Identity gRPC Service ===");
            
            // Tạo channel để test
            ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                    .usePlaintext()
                    .build();
            
            IdentityServiceGrpc.IdentityServiceBlockingStub blockingStub = IdentityServiceGrpc.newBlockingStub(channel);
            
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
            
            log.info("=== Identity gRPC Service Health Check Completed ===");
            
            channel.shutdown();
            
        } catch (Exception e) {
            log.error("Lỗi khi health check Identity gRPC", e);
        }
    }
}
