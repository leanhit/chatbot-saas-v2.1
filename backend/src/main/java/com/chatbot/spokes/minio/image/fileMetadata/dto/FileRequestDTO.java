package com.chatbot.spokes.minio.image.fileMetadata.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileRequestDTO {
    @Builder.Default
    private List<MultipartFile> files = new ArrayList<>();
    @Builder.Default
    private List<String> urls = new ArrayList<>();
    private String title;
    private String description;
    private UUID categoryId;
    @Builder.Default
    private List<String> tags = new ArrayList<>();
    private String code;  
    
    // Manual getters for Lombok compatibility issues
    public List<MultipartFile> getFiles() { return files; }
    public List<String> getUrls() { return urls; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public UUID getCategoryId() { return categoryId; }
    public List<String> getTags() { return tags; }
    public String getCode() { return code; }
    
    // Manual setters
    public void setFiles(List<MultipartFile> files) { this.files = files; }
    public void setUrls(List<String> urls) { this.urls = urls; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public void setCode(String code) { this.code = code; }
}
