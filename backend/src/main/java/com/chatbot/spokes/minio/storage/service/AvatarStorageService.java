package com.chatbot.spokes.minio.storage.service;

import io.minio.*;
import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Avatar Storage Service
 * Specialized service for uploading avatars and tenant logos
 * Buckets are automatically created on startup by MinioBucketInitializer
 */
@Service
@Slf4j
public class AvatarStorageService {

    private final MinioClient minioClient;
    
    @Value("${minio.endpoint}")
    private String endpoint;

    public AvatarStorageService(@Value("${minio.endpoint}") String endpoint,
                              @Value("${minio.access.key}") String accessKey,
                              @Value("${minio.secret.key}") String secretKey) {
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    /**
     * Upload user avatar to dedicated bucket
     */
    public String uploadUserAvatar(String userId, MultipartFile file) {
        try {
            log.info("🔄 [AVATAR] Starting user avatar upload - userId: {}, fileName: {}, fileSize: {}", 
                    userId, file.getOriginalFilename(), file.getSize());
            
            String bucketName = "user-avatars";
            String fileExtension = getFileExtension(file.getOriginalFilename());
            String objectName = "avatar_" + userId + "_" + UUID.randomUUID() + "." + fileExtension;
            
            log.info("📁 [AVATAR] Bucket: {}, ObjectName: {}, Extension: {}", bucketName, objectName, fileExtension);
            
            // Check file size
            if (file.getSize() == 0) {
                log.warn("⚠️ [AVATAR] File is empty (size: 0), but proceeding with upload");
            }
            
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
            
            String fileUrl = getFileUrl(bucketName, objectName);
            log.info("✅ [AVATAR] User avatar uploaded successfully: userId={}, objectName={}, url={}", userId, objectName, fileUrl);
            return fileUrl;
        } catch (Exception e) {
            log.error("❌ [AVATAR] Error uploading user avatar to MinIO: userId={}, fileSize={}", userId, file.getSize(), e);
            throw new RuntimeException("Failed to upload user avatar", e);
        }
    }

    /**
     * Upload tenant logo to dedicated bucket
     */
    public String uploadTenantLogo(String tenantId, MultipartFile file) {
        try {
            log.info("🔄 [TENANT LOGO] Starting tenant logo upload - tenantId: {}, fileName: {}, fileSize: {}", 
                    tenantId, file.getOriginalFilename(), file.getSize());
            
            String bucketName = "tenant-logos";
            String fileExtension = getFileExtension(file.getOriginalFilename());
            String objectName = "logo_" + tenantId + "_" + UUID.randomUUID() + "." + fileExtension;
            
            log.info("📁 [TENANT LOGO] Bucket: {}, ObjectName: {}, Extension: {}", bucketName, objectName, fileExtension);
            
            // Check file size
            if (file.getSize() == 0) {
                log.warn("⚠️ [TENANT LOGO] File is empty (size: 0), but proceeding with upload");
            }
            
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
            
            String fileUrl = getFileUrl(bucketName, objectName);
            log.info("✅ [TENANT LOGO] Tenant logo uploaded successfully: tenantId={}, objectName={}, url={}", tenantId, objectName, fileUrl);
            return fileUrl;
        } catch (Exception e) {
            log.error("❌ [TENANT LOGO] Error uploading tenant logo to MinIO: tenantId={}, fileSize={}", tenantId, file.getSize(), e);
            throw new RuntimeException("Failed to upload tenant logo", e);
        }
    }

    /**
     * Delete user avatar
     */
    public void deleteUserAvatar(String objectName) {
        try {
            String bucketName = "user-avatars";
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
            
            log.info("User avatar deleted successfully: {}", objectName);
        } catch (Exception e) {
            log.error("Error deleting user avatar from MinIO: {}", objectName, e);
            throw new RuntimeException("Failed to delete user avatar", e);
        }
    }

    /**
     * Delete tenant logo
     */
    public void deleteTenantLogo(String objectName) {
        try {
            String bucketName = "tenant-logos";
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
            
            log.info("Tenant logo deleted successfully: {}", objectName);
        } catch (Exception e) {
            log.error("Error deleting tenant logo from MinIO: {}", objectName, e);
            throw new RuntimeException("Failed to delete tenant logo", e);
        }
    }

    /**
     * Get file URL from specific bucket
     */
    private String getFileUrl(String bucketName, String objectName) {
        return String.format("%s/%s/%s", 
            endpoint, bucketName, objectName);
    }

    /**
     * Extract file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "jpg"; // default extension
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "jpg"; // default extension
        }
        
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
}
