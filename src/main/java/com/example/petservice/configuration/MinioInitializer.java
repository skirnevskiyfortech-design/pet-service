package com.example.petservice.configuration;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MinioInitializer {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    @Value("${minio.bucket-policy:public-read}")
    private String bucketPolicy;

    @EventListener(ApplicationReadyEvent.class)
    public void createBucketOnStartup() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (exists) {
                log.info("Bucket '{}' already exists", bucketName);
            } else {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
                log.info("Bucket '{}' created successfully", bucketName);
            }

            applyBucketPolicy();

        } catch (Exception e) {
            log.error("Failed to create bucket '{}': {}", bucketName, e.getMessage(), e);
        }
    }

    private void applyBucketPolicy() throws Exception {
        String policy = switch (bucketPolicy.toLowerCase()) {
            case "public-read" -> """
                {
                  "Version": "2012-10-17",
                  "Statement": [{
                    "Effect": "Allow",
                    "Principal": {"AWS": ["*"]},
                    "Action": ["s3:GetObject"],
                    "Resource": ["arn:aws:s3:::%s/*"]
                  }]
                }
                """.formatted(bucketName);

            case "private" -> """
                {
                  "Version": "2012-10-17",
                  "Statement": []
                }
                """;

            default -> null;
        };

        if (policy != null) {
            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(policy)
                            .build()
            );
            log.info("Policy '{}' applied to bucket '{}'", bucketPolicy, bucketName);
        }
    }
}