package com.chatbot.spokes.minio.image.category.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@Builder
public class CategoryResponseDTO {
    private UUID id;
    private String name;
    private String description;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime updatedAt;
    
    private Long tenantId;
    
    // Full constructor for CategoryService
    public CategoryResponseDTO(UUID id, String name, String description, 
                              LocalDateTime createdAt, LocalDateTime updatedAt, Long tenantId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.tenantId = tenantId;
    }
}