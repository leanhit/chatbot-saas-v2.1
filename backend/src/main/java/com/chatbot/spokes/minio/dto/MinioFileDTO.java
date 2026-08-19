package com.chatbot.spokes.minio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for MinIO File - used for cross-spoke communication
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MinioFileDTO {
    private UUID id;
    private Long tenantId;
    private String fileName;
    private String originalFileName;
    private String mimeType;
    private Long fileSize;
    private String bucketName;
    private String objectName;
    private String fileUrl;
    private UUID categoryId;
    private LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;
}
