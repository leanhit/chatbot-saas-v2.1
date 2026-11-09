package com.chatbot.spokes.minio.image.category.controller;

import lombok.RequiredArgsConstructor;
import com.chatbot.spokes.minio.image.category.dto.CategoryRequestDTO;
import com.chatbot.spokes.minio.image.category.dto.CategoryResponseDTO;
import com.chatbot.spokes.minio.image.category.service.CategoryService;
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.core.tenant.service.TenantPermissionValidator;
import com.chatbot.core.tenant.exception.InsufficientPermissionException;
import com.chatbot.shared.exceptions.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public @RequiredArgsConstructor
class CategoryController {


    private final CategoryService categoryService;
    private final TenantPermissionValidator permissionValidator;

    private void requireOwnerOrEditor() {
        Long tenantId = TenantContext.getTenantId();
        String email = permissionValidator.getCurrentUserEmail();
        if (tenantId == null || email == null || !permissionValidator.isOwnerOrEditor(tenantId, email)) {
            throw new InsufficientPermissionException(ErrorCode.INSUFFICIENT_PERMISSION,
                    "Only tenant owner or editor can modify categories");
        }
    }

    // READ (Đọc tất cả danh mục)
    @GetMapping
    public List<CategoryResponseDTO> getAllCategories() {
        return categoryService.getAllCategories();
    }

    // READ (Đọc một danh mục theo ID)
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable UUID id) {
        return categoryService.getCategoryDtoById(id) // Thay đổi ở đây
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // CREATE (Tạo một danh mục mới)
    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(@Valid @RequestBody CategoryRequestDTO requestDTO) {
        requireOwnerOrEditor();
        try {
            CategoryResponseDTO createdCategory = categoryService.createCategory(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }

    // UPDATE (Cập nhật một danh mục)
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(@PathVariable UUID id, @Valid @RequestBody CategoryRequestDTO requestDTO) {
        requireOwnerOrEditor();
        try {
            CategoryResponseDTO updatedCategory = categoryService.updateCategory(id, requestDTO);
            return ResponseEntity.ok(updatedCategory);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE (Xóa một danh mục)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        requireOwnerOrEditor();
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}