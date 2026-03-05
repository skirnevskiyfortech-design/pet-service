package com.example.petservice.service;

import com.example.petservice.configuration.MinioProperties;
import com.example.petservice.model.FileMetadata;
import com.example.petservice.repository.FileMetadataRepository;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {

    private final MinioClient minioClient;
    private final FileMetadataRepository fileMetadataRepository;
    private final MinioProperties minioProperties;

    public FileMetadata uploadFile(MultipartFile file) throws Exception {
        String original = file.getOriginalFilename();
        String extension = getFileExtension(original);
        String filePath = UUID.randomUUID().toString() + extension;

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(filePath)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        }

        FileMetadata metadata = FileMetadata.builder()
                .fileName(original)
                .filePath(filePath)
                .contentType(file.getContentType())
                .size(file.getSize())
                .createdAt(LocalDateTime.now())
                .build();

        return fileMetadataRepository.save(metadata);
    }

    public void deleteFile(String fileId) throws Exception {
        Long id = Long.parseLong(fileId);

        FileMetadata metadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("File not found: " + fileId));

        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .object(metadata.getFilePath())
                        .build()
        );

        fileMetadataRepository.delete(metadata);

        log.info("Deleted file: {} from bucket: {}", metadata.getFilePath(), minioProperties.getBucket());
    }

    /*
    Сохраняет файл из InputStream в MinIO
    и записывает метаданные в бд, используется для загрузки по URL
     */
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
                        .bucket(minioProperties.getBucket())
                        .object(filePath)
                        .stream(inputStream, size, -1)
                        .contentType(contentType)
                        .build()
        );

        FileMetadata metadata = FileMetadata.builder()
                .fileName(fileName)
                .filePath(filePath)
                .contentType(contentType)
                .size(size)
                .createdAt(LocalDateTime.now())
                .build();

        return fileMetadataRepository.save(metadata);
    }

    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf("."));
        }
        return "";
    }
}
