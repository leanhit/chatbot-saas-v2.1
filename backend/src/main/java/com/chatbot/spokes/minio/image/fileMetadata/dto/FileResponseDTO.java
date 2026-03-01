package com.chatbot.spokes.minio.image.fileMetadata.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import com.chatbot.shared.utils.DateUtils;
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
    
    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    private LocalDateTime updatedAt;

    private String title;
    private String description;
    private String categoryId;
    private List<String> tags;
    private String code;   // <--- thêm mới
}
