package com.example.petservice.configuration;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value; // Важно: импортируй эту аннотацию, а не lombok.Value
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfiguration {

    @Value("${minio.url}")
    private String minioUrl;

    @Value("${minio.user}")
    private String user;

    @Value("${minio.password}")
    private String password;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(user, password)
                .build();
    }
}