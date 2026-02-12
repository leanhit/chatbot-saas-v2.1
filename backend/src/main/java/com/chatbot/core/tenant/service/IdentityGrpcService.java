package com.chatbot.core.tenant.service;

import com.chatbot.core.tenant.grpc.TenantGrpcClient;
import com.chatbot.core.identity.grpc.IdentityGrpcClient;
import com.chatbot.core.identity.grpc.IdentityServiceOuterClass.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class IdentityGrpcService {

    @Autowired
    private IdentityGrpcClient identityGrpcClient;

    /**
     * Validate JWT token với Identity Hub
     */
    public boolean validateToken(String token) {
        try {
            log.info("Tenant Hub validating token với Identity Hub");
            ValidateTokenResponse response = identityGrpcClient.validateToken(token);
            
            if (response != null) {
                log.info("Token validation result: valid={}, userId={}, email={}", 
                        response.getValid(), response.getUserId(), response.getEmail());
                return response.getValid();
            }
            
            return false;
        } catch (Exception e) {
            log.error("Lỗi khi validate token với Identity Hub", e);
            return false;
        }
    }

    /**
     * Validate user existence với Identity Hub
     */
    public boolean validateUser(String userId) {
        try {
            log.info("Tenant Hub validating user {} với Identity Hub", userId);
            ValidateUserResponse response = identityGrpcClient.validateUser(userId);
            
            if (response != null) {
                log.info("User validation result: valid={}, isActive={}, message={}", 
                        response.getValid(), response.getIsActive(), response.getMessage());
                return response.getValid();
            }
            
            return false;
        } catch (Exception e) {
            log.error("Lỗi khi validate user {} với Identity Hub", userId, e);
            return false;
        }
    }

    /**
     * Get user role từ Identity Hub
     */
    public String getUserRole(String userId) {
        try {
            log.info("Tenant Hub getting role cho user {} từ Identity Hub", userId);
            GetUserRoleResponse response = identityGrpcClient.getUserRole(userId);
            
            if (response != null && !response.getRole().isEmpty()) {
                log.info("User {} role: {}", userId, response.getRole());
                return response.getRole();
            }
            
            return null;
        } catch (Exception e) {
            log.error("Lỗi khi get role cho user {} từ Identity Hub", userId, e);
            return null;
        }
    }

    /**
     * Check user active status với Identity Hub
     */
    public boolean isUserActive(String userId) {
        try {
            log.info("Tenant Hub checking active status cho user {} với Identity Hub", userId);
            IsUserActiveResponse response = identityGrpcClient.isUserActive(userId);
            
            if (response != null) {
                log.info("User {} active status: {}", userId, response.getIsActive());
                return response.getIsActive();
            }
            
            return false;
        } catch (Exception e) {
            log.error("Lỗi khi check active status cho user {} với Identity Hub", userId, e);
            return false;
        }
    }

    /**
     * Get user profile từ Identity Hub
     */
    public GetUserResponse getUserProfile(String userId) {
        try {
            log.info("Tenant Hub getting profile cho user {} từ Identity Hub", userId);
            GetUserResponse response = identityGrpcClient.getUserProfile(userId);
            
            if (response != null) {
                log.info("Got profile cho user {}: email={}, role={}, active={}", 
                        userId, response.getEmail(), response.getRole(), response.getIsActive());
            }
            
            return response;
        } catch (Exception e) {
            log.error("Lỗi khi get profile cho user {} từ Identity Hub", userId, e);
            return null;
        }
    }
}
