package com.chatbot.spokes.minio.storage.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * MinIO Storage Service
 * Handles file storage operations with MinIO
 */
@Service
@Slf4j
public class MinioStorageService {

    private final MinioClient minioClient;
    
    @Value("${minio.bucket.name}")
    private String bucketName;
    
    @Value("${minio.endpoint}")
    private String endpoint;

    public MinioStorageService(@Value("${minio.endpoint}") String endpoint,
                              @Value("${minio.access.key}") String accessKey,
                              @Value("${minio.secret.key}") String secretKey) {
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    public String uploadFile(String objectName, MultipartFile file) {
        try {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
            
            log.info("File uploaded successfully: {}", objectName);
            return objectName;
        } catch (Exception e) {
            log.error("Error uploading file to MinIO: {}", objectName, e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    public InputStream downloadFile(String objectName) {
        try {
            return minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
        } catch (Exception e) {
            log.error("Error downloading file from MinIO: {}", objectName, e);
            throw new RuntimeException("Failed to download file", e);
        }
    }

    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
            
            log.info("File deleted successfully: {}", objectName);
        } catch (Exception e) {
            log.error("Error deleting file from MinIO: {}", objectName, e);
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    public String getFileUrl(String objectName) {
        return String.format("%s/%s/%s", 
            endpoint, bucketName, objectName);
    }
}
