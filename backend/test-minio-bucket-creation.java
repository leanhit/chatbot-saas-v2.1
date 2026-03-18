import io.minio.*;
import io.minio.errors.*;

public class TestMinioBucketCreation {
    public static void main(String[] args) {
        try {
            // Initialize MinIO client
            MinioClient minioClient = MinioClient.builder()
                .endpoint("http://localhost:9000")
                .credentials("minioadmin", "minioadmin")
                .build();
            
            // Test bucket creation
            String[] buckets = {"chatbot-files", "user-avatars", "tenant-logos"};
            
            for (String bucketName : buckets) {
                boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                        .bucket(bucketName)
                        .build()
                );
                
                System.out.println("Bucket '" + bucketName + "' exists: " + bucketExists);
                
                if (!bucketExists) {
                    System.out.println("Creating bucket: " + bucketName);
                    minioClient.makeBucket(
                        MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .build()
                    );
                    System.out.println("Bucket created successfully: " + bucketName);
                    
                    // Set public read policy
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
                    System.out.println("Bucket policy set for: " + bucketName);
                }
            }
            
            // List all buckets
            System.out.println("\nAll buckets:");
            minioClient.listBuckets().forEach(bucket -> {
                System.out.println("- " + bucket.name());
            });
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
