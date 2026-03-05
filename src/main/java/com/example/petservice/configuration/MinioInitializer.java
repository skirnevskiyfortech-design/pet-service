package com.example.petservice.configuration;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MinioInitializer {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @EventListener(ApplicationReadyEvent.class)
    public void createBucketOnStartup() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .build()
            );

            if (exists) {
                log.info("Bucket '{}' already exists",
                        minioProperties.getBucket());
            } else {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(minioProperties.getBucket())
                                .build()
                );
                log.info("Bucket '{}' created successfully",
                        minioProperties.getBucket());
            }

            applyBucketPolicy();

        } catch (Exception e) {
            log.error("Failed to create bucket '{}': {}",
                    minioProperties.getBucket(),
                    e.getMessage(), e);
        }
    }

    private void applyBucketPolicy() throws Exception {
        String policy = switch (minioProperties.getBucketPolicy().toLowerCase()) {
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
                    """.formatted(minioProperties.getBucket());

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
                            .bucket(minioProperties.getBucket())
                            .config(policy)
                            .build()
            );
            log.info("Policy '{}' applied to bucket '{}'",
                    minioProperties.getBucketPolicy(),
                    minioProperties.getBucket());
        }
    }
}