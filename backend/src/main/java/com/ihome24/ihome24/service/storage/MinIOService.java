package com.ihome24.ihome24.service.storage;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinIOService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.initialize-on-startup:true}")
    private boolean initializeOnStartup;

    private volatile boolean bucketInitialized = false;

    @PostConstruct
    private void initializeBucket() {
        if (!initializeOnStartup) {
            log.info("MinIO bucket initialization skipped (initialize-on-startup=false)");
            return;
        }

        try {
            ensureBucketExists();
        } catch (Exception e) {
            log.warn("Failed to initialize MinIO bucket on startup: {}. Bucket will be created on first use.", e.getMessage());
            // Не бросаем исключение, чтобы приложение могло запуститься
            // Bucket будет создан при первой загрузке файла
        }
    }

    private void ensureBucketExists() {
        if (bucketInitialized) {
            return;
        }

        synchronized (this) {
            if (bucketInitialized) {
                return;
            }

            try {
                boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                        .bucket(bucketName)
                        .build());
                if (!found) {
                    minioClient.makeBucket(MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .build());
                    log.info("Bucket '{}' created successfully", bucketName);
                } else {
                    log.info("Bucket '{}' already exists", bucketName);
                }
                bucketInitialized = true;
            } catch (Exception e) {
                log.error("Error checking/creating bucket: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to initialize MinIO bucket", e);
            }
        }
    }

    public void uploadFile(String objectName, InputStream inputStream, String contentType, long size) {
        ensureBucketExists();
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build());
            log.debug("File uploaded successfully: {}", objectName);
        } catch (Exception e) {
            log.error("Error uploading file {}: {}", objectName, e.getMessage(), e);
            throw new RuntimeException("Failed to upload file to MinIO", e);
        }
    }

    public void uploadFile(String objectName, byte[] data, String contentType) {
        uploadFile(objectName, new ByteArrayInputStream(data), contentType, data.length);
    }

    public InputStream getFile(String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("Error getting file {}: {}", objectName, e.getMessage(), e);
            throw new RuntimeException("Failed to get file from MinIO", e);
        }
    }

    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            log.debug("File deleted successfully: {}", objectName);
        } catch (Exception e) {
            log.error("Error deleting file {}: {}", objectName, e.getMessage(), e);
            throw new RuntimeException("Failed to delete file from MinIO", e);
        }
    }

    public boolean fileExists(String objectName) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getFileUrl(String objectName) {
        // В продакшене можно использовать presigned URL
        // Для разработки возвращаем путь к API endpoint
        return "/api/files/" + objectName;
    }

    public String getBucketName() {
        return bucketName;
    }
}
