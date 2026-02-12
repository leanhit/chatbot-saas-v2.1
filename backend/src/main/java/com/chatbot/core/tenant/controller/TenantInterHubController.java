package com.chatbot.core.tenant.controller;

import com.chatbot.core.tenant.service.IdentityGrpcService;
import com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tenant-inter-hub")
@Slf4j
public class TenantInterHubController {

    @Autowired
    private IdentityGrpcService identityGrpcService;

    /**
     * Tenant Hub gọi Identity Hub để validate token
     */
    @PostMapping("/tenant/validate-token")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            boolean isValid = identityGrpcService.validateToken(token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            response.put("message", isValid ? "Token hợp lệ" : "Token không hợp lệ");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi validate token", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Tenant Hub gọi Identity Hub để validate user
     */
    @PostMapping("/tenant/validate-user")
    public ResponseEntity<Map<String, Object>> validateUser(@RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");
            boolean isValid = identityGrpcService.validateUser(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            response.put("userId", userId);
            response.put("message", isValid ? "User hợp lệ" : "User không hợp lệ");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi validate user", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Tenant Hub gọi Identity Hub để get user role
     */
    @GetMapping("/tenant/user-role/{userId}")
    public ResponseEntity<Map<String, Object>> getUserRole(@PathVariable String userId) {
        try {
            String role = identityGrpcService.getUserRole(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("role", role);
            response.put("message", role != null ? "Lấy role thành công" : "Không tìm thấy role");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi get user role", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Tenant Hub gọi Identity Hub để check user active
     */
    @GetMapping("/tenant/user-active/{userId}")
    public ResponseEntity<Map<String, Object>> isUserActive(@PathVariable String userId) {
        try {
            boolean isActive = identityGrpcService.isUserActive(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("isActive", isActive);
            response.put("message", isActive ? "User đang active" : "User không active");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi check user active", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Tenant Hub gọi Identity Hub để get user profile
     */
    @GetMapping("/tenant/user-profile/{userId}")
    public ResponseEntity<GetUserResponse> getUserProfile(@PathVariable String userId) {
        try {
            GetUserResponse profile = identityGrpcService.getUserProfile(userId);
            
            if (profile != null) {
                return ResponseEntity.ok(profile);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Lỗi khi get user profile", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
