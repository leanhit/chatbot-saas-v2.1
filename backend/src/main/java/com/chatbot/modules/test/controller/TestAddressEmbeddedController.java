package com.chatbot.modules.test.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Test Controller cho User/Tenant với Address Embedded Object
 */
@RestController
@RequestMapping("/api/test/address-embedded")
public class TestAddressEmbeddedController {
    
    /**
     * Test tạo User với embedded address
     */
    @PostMapping("/user")
    public ResponseEntity<Map<String, Object>> createUserWithAddress(@RequestBody Map<String, Object> request) {
        String fullName = (String) request.get("fullName");
        String phoneNumber = (String) request.get("phoneNumber");
        
        UUID userId = UUID.randomUUID();
        
        Map<String, Object> address = Map.of(
            "houseNumber", request.get("houseNumber"),
            "street", request.get("street"),
            "ward", request.get("ward"),
            "district", request.get("district"),
            "province", request.get("province"),
            "country", request.getOrDefault("country", "Việt Nam"),
            "isDefault", request.getOrDefault("isDefault", true),
            "fullAddress", String.format("%s %s, %s, %s, %s, %s", 
                request.get("houseNumber"), request.get("street"), request.get("ward"), 
                request.get("district"), request.get("province"), request.getOrDefault("country", "Việt Nam"))
        );
        
        Map<String, Object> user = new java.util.HashMap<>();
        user.put("id", userId.toString());
        user.put("fullName", fullName);
        user.put("phoneNumber", phoneNumber);
        user.put("address", address);
        user.put("message", "User with embedded address created successfully");
        user.put("createdAt", System.currentTimeMillis());
        
        return ResponseEntity.ok(user);
    }
    
    /**
     * Test tạo Tenant với embedded address
     */
    @PostMapping("/tenant")
    public ResponseEntity<Map<String, Object>> createTenantWithAddress(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        String domain = (String) request.get("domain");
        
        UUID tenantId = UUID.randomUUID();
        
        Map<String, Object> address = Map.of(
            "houseNumber", request.get("houseNumber"),
            "street", request.get("street"),
            "ward", request.get("ward"),
            "district", request.get("district"),
            "province", request.get("province"),
            "country", request.getOrDefault("country", "Việt Nam"),
            "isDefault", request.getOrDefault("isDefault", true),
            "fullAddress", String.format("%s %s, %s, %s, %s, %s", 
                request.get("houseNumber"), request.get("street"), request.get("ward"), 
                request.get("district"), request.get("province"), request.getOrDefault("country", "Việt Nam"))
        );
        
        Map<String, Object> tenant = new java.util.HashMap<>();
        tenant.put("id", tenantId.toString());
        tenant.put("name", name);
        tenant.put("domain", domain);
        tenant.put("status", "ACTIVE");
        tenant.put("address", address);
        tenant.put("message", "Tenant with embedded address created successfully");
        tenant.put("createdAt", System.currentTimeMillis());
        
        return ResponseEntity.ok(tenant);
    }
    
    /**
     * Test so sánh giữa mảng addresses và embedded address
     */
    @GetMapping("/comparison")
    public ResponseEntity<Map<String, Object>> compareAddressModels() {
        Map<String, Object> arrayModel = new java.util.HashMap<>();
        arrayModel.put("type", "List<Address>");
        arrayModel.put("description", "User có nhiều địa chỉ, lưu trong bảng addresses riêng");
        arrayModel.put("pros", "Hỗ trợ nhiều địa chỉ, linh hoạt");
        arrayModel.put("cons", "Phức tạp, cần JOIN query, khó quản lý");
        arrayModel.put("useCase", "User có nhiều địa chỉ giao hàng, billing khác nhau");
        
        Map<String, Object> embeddedModel = new java.util.HashMap<>();
        embeddedModel.put("type", "AddressEmbedded");
        embeddedModel.put("description", "User có 1 địa chỉ chính, nhúng trong user table");
        embeddedModel.put("pros", "Đơn giản, nhanh query, dễ quản lý");
        embeddedModel.put("cons", "Chỉ hỗ trợ 1 địa chỉ, cần migrate data");
        embeddedModel.put("useCase", "User chỉ có 1 địa chỉ chính, tenant có 1 địa chỉ văn phòng");
        
        Map<String, Object> comparison = new java.util.HashMap<>();
        comparison.put("arrayModel", arrayModel);
        comparison.put("embeddedModel", embeddedModel);
        comparison.put("recommendation", "Dùng embedded model cho tenant và user info chính, dùng array model cho addresses phụ nếu cần");
        
        return ResponseEntity.ok(comparison);
    }
}
