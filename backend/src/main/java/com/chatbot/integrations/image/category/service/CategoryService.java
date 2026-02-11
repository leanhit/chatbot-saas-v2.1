package com.chatbot.integrations.image.category.service;

import com.chatbot.integrations.image.category.dto.CategoryRequestDTO;
import com.chatbot.integrations.image.category.dto.CategoryResponseDTO;
import com.chatbot.integrations.image.category.model.Category; // Correct import for Category model
import com.chatbot.integrations.image.category.repository.CategoryRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // --- Entity -> DTO ---
    private CategoryResponseDTO convertToResponseDTO(Category category) {
        return new CategoryResponseDTO(
            category.getId(),
            category.getName(),
            category.getDescription(),
            category.getCreatedAt(),
            category.getUpdatedAt(),
            category.getTenantId()
        );
    }
    
    // --- CREATE ---
    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO requestDTO) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context khi tạo danh mục");
        }
        
        if (categoryRepository.findByNameAndTenantId(requestDTO.getName(), tenantId).isPresent()) {
            throw new IllegalArgumentException("Danh mục đã tồn tại với tên: " + requestDTO.getName());
        }
        Category newCategory = new Category();
        newCategory.setName(requestDTO.getName());
        newCategory.setDescription(requestDTO.getDescription());
        // tenantId sẽ được tự động gán trong @PrePersist của BaseTenantEntity
        Category savedCategory = categoryRepository.save(newCategory);
        return convertToResponseDTO(savedCategory);
    }

    // --- CREATE GLOBAL (no tenant context) ---
    @Transactional
    public CategoryResponseDTO createCategoryGlobal(CategoryRequestDTO requestDTO) {
        // Check if global category already exists
        if (categoryRepository.findByNameAndTenantIdIsNull(requestDTO.getName()).isPresent()) {
            throw new IllegalArgumentException("Danh mục global đã tồn tại với tên: " + requestDTO.getName());
        }
        Category newCategory = new Category();
        newCategory.setName(requestDTO.getName());
        newCategory.setDescription(requestDTO.getDescription());
        newCategory.setTenantId(null); // Explicitly set to null for global category
        Category savedCategory = categoryRepository.save(newCategory);
        return convertToResponseDTO(savedCategory);
    }

    // --- READ all ---
    public List<CategoryResponseDTO> getAllCategories() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context khi lấy danh sách danh mục");
        }
        
        return categoryRepository.findAllByTenantId(tenantId).stream()
            .map(this::convertToResponseDTO)
            .collect(Collectors.toList());
    }

    // --- READ all GLOBAL (no tenant context) ---
    public List<CategoryResponseDTO> getAllCategoriesGlobal() {
        return categoryRepository.findAllByTenantIdIsNull().stream()
            .map(this::convertToResponseDTO)
            .collect(Collectors.toList());
    }

    // --- READ by ID (DTO cho client) ---
    public Optional<CategoryResponseDTO> getCategoryDtoById(UUID id) {
        return categoryRepository.findById(id).map(this::convertToResponseDTO);
    }

    // --- READ by ID (Entity cho nội bộ Service) ---
    public Optional<Category> getCategoryById(UUID id) {
        return categoryRepository.findById(id);
    }
    
    // --- UPDATE ---
    @Transactional
    public CategoryResponseDTO updateCategory(UUID id, CategoryRequestDTO requestDTO) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context khi cập nhật danh mục");
        }
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với id: " + id));
        
        // Kiểm tra xem category có thuộc tenant hiện tại không
        if (!category.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Không có quyền cập nhật danh mục này");
        }
        
        category.setName(requestDTO.getName());
        category.setDescription(requestDTO.getDescription());
        category.setUpdatedAt(LocalDateTime.now());
        
        Category updatedCategory = categoryRepository.save(category);
        return convertToResponseDTO(updatedCategory);
    }

    // --- DELETE ---
    @Transactional
    public void deleteCategory(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy danh mục với id: " + id);
        }
        categoryRepository.deleteById(id);
    }
}
