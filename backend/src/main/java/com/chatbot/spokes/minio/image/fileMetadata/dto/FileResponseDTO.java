package com.chatbot.spokes.minio.image.fileMetadata.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileResponseDTO {
    private UUID id;
    private String fileName;
    private String fileUrl;
    private long fileSize;
    private String contentType;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime updatedAt;

    private String title;
    private String description;
    private String categoryId;
    private List<String> tags;
    private String code;   // <--- thêm mới
}
