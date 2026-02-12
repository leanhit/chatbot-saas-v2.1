package com.chatbot.core.identity.service;

import com.chatbot.core.tenant.grpc.TenantGrpcClient;
import com.chatbot.core.tenant.grpc.TenantServiceProto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TenantGrpcService {

    @Autowired
    private TenantGrpcClient tenantGrpcClient;

    /**
     * Validate tenant với Tenant Hub
     */
    public boolean validateTenant(String tenantKey) {
        try {
            log.info("Identity Hub validating tenant {} với Tenant Hub", tenantKey);
            ValidateTenantResponse response = tenantGrpcClient.validateTenant(tenantKey);
            
            if (response != null) {
                log.info("Tenant validation result: valid={}, status={}, message={}", 
                        response.getValid(), response.getStatus(), response.getMessage());
                return response.getValid();
            }
            
            return false;
        } catch (Exception e) {
            log.error("Lỗi khi validate tenant {} với Tenant Hub", tenantKey, e);
            return false;
        }
    }

    /**
     * Check tenant exists với Tenant Hub
     */
    public boolean checkTenantExists(String tenantKey) {
        try {
            log.info("Identity Hub checking tenant {} exists với Tenant Hub", tenantKey);
            CheckTenantExistsResponse response = tenantGrpcClient.checkTenantExists(tenantKey);
            
            if (response != null) {
                log.info("Tenant {} exists: {}", tenantKey, response.getExists());
                return response.getExists();
            }
            
            return false;
        } catch (Exception e) {
            log.error("Lỗi khi check tenant {} exists với Tenant Hub", tenantKey, e);
            return false;
        }
    }

    /**
     * Get tenant information từ Tenant Hub
     */
    public TenantDetailResponse getTenant(String tenantKey) {
        try {
            log.info("Identity Hub getting tenant {} từ Tenant Hub", tenantKey);
            TenantDetailResponse response = tenantGrpcClient.getTenant(tenantKey);
            
            if (response != null) {
                log.info("Got tenant {}: name={}, status={}, visibility={}", 
                        tenantKey, response.getName(), response.getStatus(), response.getVisibility());
            }
            
            return response;
        } catch (Exception e) {
            log.error("Lỗi khi get tenant {} từ Tenant Hub", tenantKey, e);
            return null;
        }
    }

    /**
     * List all tenants từ Tenant Hub
     */
    public ListTenantsResponse listTenants(int page, int size) {
        try {
            log.info("Identity Hub listing tenants từ Tenant Hub - page: {}, size: {}", page, size);
            ListTenantsResponse response = tenantGrpcClient.listTenants(page, size);
            
            if (response != null) {
                log.info("Listed tenants: totalElements={}, totalPages={}", 
                        response.getTotalElements(), response.getTotalPages());
            }
            
            return response;
        } catch (Exception e) {
            log.error("Lỗi khi list tenants từ Tenant Hub", e);
            return null;
        }
    }

    /**
     * Create tenant với Tenant Hub
     */
    public TenantResponse createTenant(String name, String description, String tenantKey, String visibility, String ownerEmail) {
        try {
            log.info("Identity Hub creating tenant {} với Tenant Hub", name);
            
            CreateTenantRequest request = CreateTenantRequest.newBuilder()
                    .setName(name)
                    .setDescription(description)
                    .setTenantKey(tenantKey)
                    .setVisibility(visibility)
                    .setOwnerEmail(ownerEmail)
                    .build();
            
            TenantResponse response = tenantGrpcClient.createTenant(request);
            
            if (response != null) {
                log.info("Created tenant: id={}, key={}, name={}", 
                        response.getId(), response.getTenantKey(), response.getName());
            }
            
            return response;
        } catch (Exception e) {
            log.error("Lỗi khi create tenant {} với Tenant Hub", name, e);
            return null;
        }
    }
}
