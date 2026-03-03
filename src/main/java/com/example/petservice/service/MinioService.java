package com.example.petservice.service;

import com.example.petservice.model.FileMetadata;
import com.example.petservice.repository.FileMetadataRepository;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final FileMetadataRepository fileMetadataRepository;

    @Value("${minio.bucket}")
    private String bucketName;

    public FileMetadata uploadFile(MultipartFile file) throws Exception {
        String original = file.getOriginalFilename();
        String extension = getFileExtension(original);
        String filePath = UUID.randomUUID().toString() + extension;

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        }

        FileMetadata metadata = new FileMetadata();
        metadata.setFileName(original);
        metadata.setFilePath(filePath);
        metadata.setContentType(file.getContentType());
        metadata.setSize(file.getSize());
        metadata.setCreatedAt(LocalDateTime.now());

        return fileMetadataRepository.save(metadata);
    }


// Сохраняет файл из InputStream в MinIO и записывает метаданные в бд, используется для загрузки по URL
    public FileMetadata saveFileFromData(
            InputStream inputStream,
            String fileName,
            String contentType,
            long size
    ) throws Exception {
        String extension = getFileExtension(fileName);
        String filePath = UUID.randomUUID().toString() + extension;

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(filePath)
                        .stream(inputStream, size, -1)
                        .contentType(contentType)
                        .build()
        );

        FileMetadata metadata = new FileMetadata();
        metadata.setFileName(fileName);
        metadata.setFilePath(filePath);
        metadata.setContentType(contentType);
        metadata.setSize(size);
        metadata.setCreatedAt(LocalDateTime.now());

        return fileMetadataRepository.save(metadata);
    }

    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf("."));
        }
        return "";
    }
}
