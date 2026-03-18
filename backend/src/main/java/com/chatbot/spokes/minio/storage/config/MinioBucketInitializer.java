package com.chatbot.spokes.minio.storage.config;

import io.minio.*;
import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO Bucket Initializer
 * Automatically creates required buckets on application startup
 */
@Configuration
@Slf4j
public class MinioBucketInitializer {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access.key}")
    private String accessKey;

    @Value("${minio.secret.key}")
    private String secretKey;

    @Value("${minio.bucket.name}")
    private String defaultBucket;

    @Bean("minioBucketInitializerRunner")
    public ApplicationRunner minioBucketInitializerRunner() {
        return args -> {
            log.info("Initializing MinIO buckets...");
            
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();

            // List of required buckets
            String[] requiredBuckets = {
                defaultBucket,      // chatbot-files
                "user-avatars",     // User avatar bucket
                "tenant-logos"      // Tenant logo bucket
            };

            for (String bucketName : requiredBuckets) {
                try {
                    ensureBucketExists(minioClient, bucketName);
                } catch (Exception e) {
                    log.error("Failed to create bucket: {}", bucketName, e);
                    // Don't throw exception to allow application to start
                    // Buckets will be created on-demand when needed
                }
            }

            log.info("MinIO bucket initialization completed");
        };
    }

    /**
     * Ensures bucket exists, creates it if it doesn't
     */
    private void ensureBucketExists(MinioClient minioClient, String bucketName) throws Exception {
        try {
            boolean bucketExists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build()
            );
            
            if (!bucketExists) {
                log.info("Creating bucket: {}", bucketName);
                minioClient.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build()
                );
                log.info("Bucket created successfully: {}", bucketName);
                
                // Set bucket policy to allow public read access
                String policy = """
                    {
                        "Version": "2012-10-17",
                        "Statement": [
                            {
                                "Effect": "Allow",
                                "Principal": {"AWS": ["*"]},
                                "Action": ["s3:GetObject"],
                                "Resource": ["arn:aws:s3:::%s/*"]
                            }
                        ]
                    }
                    """.formatted(bucketName);
                
                minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                        .bucket(bucketName)
                        .config(policy)
                        .build()
                );
                
                log.info("Public read policy set for bucket: {}", bucketName);
            } else {
                log.debug("Bucket already exists: {}", bucketName);
            }
        } catch (MinioException e) {
            log.error("MinIO error while creating bucket {}: {}", bucketName, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while creating bucket {}: {}", bucketName, e.getMessage());
            throw e;
        }
    }
}
