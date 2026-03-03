package com.example.petservice.model;


import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_metadata")
@Data
@RequiredArgsConstructor
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String filePath; // путь в minio

    @Column(nullable = false)
    private String contentType; // типо .jpg → image/jpeg или .png → image/png

    @Column(nullable = false)
    private Long size;

    @Column
    private String additionalMetadata;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
