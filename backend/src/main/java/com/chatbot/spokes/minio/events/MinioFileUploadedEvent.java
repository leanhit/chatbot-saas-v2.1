package com.chatbot.spokes.minio.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Kafka event published when a file is uploaded to MinIO
 * Other spokes can consume this event to process uploaded files
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MinioFileUploadedEvent {
    private UUID fileId;
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
    private final String eventType = "MINIO_FILE_UPLOADED";
}
