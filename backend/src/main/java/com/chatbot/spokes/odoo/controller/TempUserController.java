package com.chatbot.spokes.odoo.controller;

import com.chatbot.spokes.odoo.model.FbCustomerStaging;
import com.chatbot.spokes.odoo.service.FbCustomerStagingCrudService;
import com.chatbot.spokes.odoo.dto.UpdateDataRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
// import io.swagger.v3.oas.annotations.responses.ApiResponse; // Use fully qualified name to avoid conflict
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal; 
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/temp-users")
@RequiredArgsConstructor
@Tag(name = "Temp Users", description = "Temporary user management")
public class TempUserController {

    private final FbCustomerStagingCrudService service;

    // 🚩 Đã sửa: Trả về String thay vì Long
    private String getOwnerIdFromPrincipal(Principal principal) {
        if (principal == null || principal.getName() == null) {
            log.error("⚠️ Principal is missing or user is unauthenticated.");
            throw new SecurityException("User must be authenticated."); 
        }
        // Trả về username (email) làm Owner ID
        return principal.getName();
    }


    /**
     * 🧩 Lấy toàn bộ user tạm của owner hiện tại
     */
    @GetMapping
    public ResponseEntity<List<FbCustomerStaging>> getAllByOwner(
            Principal principal // Lấy ownerId từ Principal
    ) {
        try {
            // 🚩 Đã sửa kiểu dữ liệu ownerId thành String
            String ownerId = getOwnerIdFromPrincipal(principal); 
            log.info("📥 Fetching all temp users for ownerId={}", ownerId);
            // LƯU Ý: service.getAllByOwnerId() cũng phải được sửa để nhận String
            List<FbCustomerStaging> users = service.getAllByOwnerId(ownerId); 
            return ResponseEntity.ok(users);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401
        }
    }

    /**
     * 🧩 Lấy thông tin user cụ thể theo psid + ownerId
     */
    @GetMapping("/{psid}")
    @Operation(
        summary = "Get temp user by PSID",
        description = "Retrieve a specific temporary user by PSID and owner",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Temp user retrieved successfully",
                content = @Content(schema = @Schema(implementation = FbCustomerStaging.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Temp user not found")
        }
    )
    public ResponseEntity<FbCustomerStaging> getByPsid(
            @PathVariable String psid,
            Principal principal // Lấy ownerId từ Principal
    ) {
        try {
            // 🚩 Đã sửa kiểu dữ liệu ownerId thành String
            String ownerId = getOwnerIdFromPrincipal(principal); 
            // LƯU Ý: service.getByPsid() cũng phải được sửa để nhận String
            Optional<FbCustomerStaging> userOpt = service.getByPsid(psid, ownerId); 
            return userOpt.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401
        }
    }

    /**
     * 🧩 Tạo mới hoặc cập nhật (upsert)
     */
    @PostMapping
    @Operation(
        summary = "Create or update temp user",
        description = "Create new temporary user or update existing one",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Temp user created/updated successfully",
                content = @Content(schema = @Schema(implementation = FbCustomerStaging.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Cannot modify other users' data")
        }
    )
    public ResponseEntity<FbCustomerStaging> upsert(
            @RequestBody FbCustomerStaging customer,
            Principal principal
    ) {
        try {
            // 🚩 Đã sửa kiểu dữ liệu ownerId thành String
            String currentOwnerId = getOwnerIdFromPrincipal(principal);
            
            // **BẢO MẬT:** Ép buộc ownerId trong request phải là ownerId của người dùng hiện tại
            // LƯU Ý: customer.getOwnerId() cũng phải trả về String
            if (!currentOwnerId.equals(customer.getOwnerId())) { 
                 log.warn("⚠️ Unauthorized attempt to upsert user for different owner. Request ownerId={}, Principal ownerId={}", customer.getOwnerId(), currentOwnerId);
                 return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403
            }

            log.info("💾 Upserting temp user psid={} ownerId={}", customer.getPsid(), currentOwnerId);
            FbCustomerStaging saved = service.upsert(customer);
            return ResponseEntity.ok(saved);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401
        }
    }

    /**
     * 🧩 Xóa user theo psid + ownerId
     */
    @DeleteMapping("/{psid}")
    @Operation(
        summary = "Delete temp user",
        description = "Delete a temporary user by PSID and owner",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Temp user deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Temp user not found")
        }
    )
    public ResponseEntity<Void> delete(
            @PathVariable String psid,
            Principal principal // Lấy ownerId từ Principal
    ) {
        try {
            // 🚩 Đã sửa kiểu dữ liệu ownerId thành String
            String ownerId = getOwnerIdFromPrincipal(principal);
            log.warn("🗑 Deleting temp user psid={} ownerId={}", psid, ownerId);
            // LƯU Ý: service.delete() cũng phải được sửa để nhận String
            service.delete(psid, ownerId); 
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401
        }
    }

    /**
     * 🧩 Cập nhật riêng dataJson và status của user tạm theo psid + ownerId
     */
    @PatchMapping("/{psid}")
    @Operation(
        summary = "Update temp user data and status",
        description = "Update JSON data and status for a temporary user",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Temp user updated successfully",
                content = @Content(schema = @Schema(implementation = FbCustomerStaging.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid update data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Temp user not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public ResponseEntity<FbCustomerStaging> updateDataJsonAndStatus(
            @PathVariable String psid,
            @RequestBody UpdateDataRequest updateRequest,
            Principal principal
    ) {
        try {
            // ✅ Lấy ownerId từ Principal
            String ownerId = getOwnerIdFromPrincipal(principal);
            log.info("🛠 Updating dataJson/status for psid={} ownerId={} with dataJson={}", psid, ownerId, updateRequest.getDataJson());

            // 🚩 Gọi service thực hiện update (service sẽ xử lý validation và lưu DB)
            FbCustomerStaging updated = service.updateDataJsonAndStatus(
                    psid,
                    ownerId,
                    updateRequest.getDataJson(),
                    updateRequest.getStatus()
            );

            return ResponseEntity.ok(updated);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ {}", e.getMessage());
            return ResponseEntity.badRequest().build(); // 400
        } catch (Exception e) {
            log.error("💥 Error updating dataJson/status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500
        }
    }
}
