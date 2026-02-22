package com.chatbot.spokes.minio.image.category.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequestDTO {
    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;
    
    private String description;
}