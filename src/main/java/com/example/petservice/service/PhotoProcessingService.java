package com.example.petservice.service;

import com.example.petservice.configuration.PhotoProperties;
import com.example.petservice.dto.FileData;
import com.example.petservice.exception.FileTooLargeException;
import com.example.petservice.model.FileMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;


@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoProcessingService {

    private final PhotoProperties photoProperties;
    private final MinioService minioService;

    public FileData downloadFromUrl(String url) throws Exception {
        validateUrl(url);
        /*
        загрузка файла
        */
        byte[] fileData = downloadFile(url);

        if (fileData.length > photoProperties.getMaxFileSize()) {
            throw new FileTooLargeException(
                    "File size exceeds limit: " + fileData.length
                            + " bytes (max. " + photoProperties.getMaxFileSize() + ")"
            );
        }

        String fileName = extractFileName(url);

        String contentType = "image/jpeg";

        return new FileData(
                fileName,
                contentType,
                fileData.length,
                fileData
        );
    }

    public String uploadImageFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("Image URL is required");
        }

        try {
            FileData fileData = downloadFromUrl(imageUrl);

            FileMetadata savedFile = minioService.saveFileFromData(
                    fileData.toInputStream(),
                    fileData.fileName(),
                    fileData.contentType(),
                    fileData.size()
            );

            return savedFile.getId().toString();
        } catch (Exception e) {
            throw new IllegalArgumentException("Error downloading file from URL: "
                    + e.getMessage(), e);
        }
    }

    /*
    валидация url для безопасности
     */
    private void validateUrl(String urlString) throws Exception {
        URL url = new URL(urlString);

        String protocol = url.getProtocol();
        if (!"http".equalsIgnoreCase(protocol) && !"https".equalsIgnoreCase(protocol)) {
            throw new IllegalArgumentException("Only http and https protocols are allowed");
        }

        String host = url.getHost();
        InetAddress[] addresses = InetAddress.getAllByName(host);
        for (InetAddress addr : addresses) {
            if (isPrivateIp(addr)) {
                throw new IllegalArgumentException("Access to local addresses is forbidden");
            }
        }
    }

    /*
     Проверка является ли IP-адрес частным/локальным
     */
    private boolean isPrivateIp(InetAddress address) {
        return address.isLoopbackAddress()
                || address.isAnyLocalAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress();
    }

    /*
    Загрузка файла через HttpClient
     */
    private byte[] downloadFile(String url) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(photoProperties.getConnectionTimeout()))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .timeout(Duration.ofMillis(photoProperties.getReadTimeout()))
                .GET()
                .build();

        HttpResponse<byte[]> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofByteArray()
        );

        if (response.statusCode() != 200) {
            throw new IllegalArgumentException(
                    "File download error: HTTP " + response.statusCode()
            );
        }

        return response.body();
    }

    private String extractFileName(String url) {
        String path = java.net.URI.create(url).getPath();
        String fileName = path.substring(path.lastIndexOf('/') + 1);
        if (fileName.isEmpty() || fileName.contains("?")) {
            fileName = "image_" + System.currentTimeMillis() + ".jpg";
        }
        return fileName;
    }
}
