package com.ihome24.ihome24.service.storage;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinIOService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String defaultBucketName;

    @Value("${minio.product-images-bucket:product-images}")
    private String productImagesBucket;

    @Value("${minio.product-videos-bucket:product-videos}")
    private String productVideosBucket;

    @Value("${minio.initialize-on-startup:true}")
    private boolean initializeOnStartup;

    @Value("${minio.presigned-url-enabled:false}")
    private boolean presignedUrlEnabled;

    @Value("${minio.presigned-url-expiry-seconds:3600}")
    private int presignedUrlExpirySeconds;

    private final Set<String> initializedBuckets = ConcurrentHashMap.newKeySet();

    @PostConstruct
    private void initializeBucket() {
        if (!initializeOnStartup) {
            log.info("MinIO bucket initialization skipped (initialize-on-startup=false)");
            return;
        }

        try {
            ensureBucketExists(defaultBucketName);
            ensureBucketExists(productImagesBucket);
            ensureBucketExists(productVideosBucket);
        } catch (Exception e) {
            log.warn("Failed to initialize MinIO bucket on startup: {}. Bucket will be created on first use.", e.getMessage());
            // Не бросаем исключение, чтобы приложение могло запуститься
            // Bucket будет создан при первой загрузке файла
        }
    }

    private void ensureBucketExists(String bucketName) {
        if (initializedBuckets.contains(bucketName)) {
            return;
        }

        synchronized (this) {
            if (initializedBuckets.contains(bucketName)) {
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
                initializedBuckets.add(bucketName);
            } catch (Exception e) {
                log.error("Error checking/creating bucket: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to initialize MinIO bucket", e);
            }
        }
    }

    public void uploadFile(String bucketName, String objectName, InputStream inputStream, String contentType, long size) {
        ensureBucketExists(bucketName);
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

    public void uploadFile(String bucketName, String objectName, byte[] data, String contentType) {
        uploadFile(bucketName, objectName, new ByteArrayInputStream(data), contentType, data.length);
    }

    public void uploadFile(String objectName, InputStream inputStream, String contentType, long size) {
        uploadFile(defaultBucketName, objectName, inputStream, contentType, size);
    }

    public void uploadFile(String objectName, byte[] data, String contentType) {
        uploadFile(defaultBucketName, objectName, data, contentType);
    }

    public InputStream getFile(String bucketName, String objectName) {
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

    public InputStream getFile(String objectName) {
        return getFile(defaultBucketName, objectName);
    }

    public void deleteFile(String bucketName, String objectName) {
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

    public void deleteFile(String objectName) {
        deleteFile(defaultBucketName, objectName);
    }

    public boolean fileExists(String bucketName, String objectName) {
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

    public boolean fileExists(String objectName) {
        return fileExists(defaultBucketName, objectName);
    }

    public String getFileUrl(String bucketName, String objectName) {
        if (presignedUrlEnabled) {
            try {
                return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .method(Method.GET)
                        .expiry(presignedUrlExpirySeconds)
                        .build());
            } catch (Exception e) {
                log.warn("Failed to generate presigned URL for {}: {}", objectName, e.getMessage());
            }
        }
        return "/api/files/" + bucketName + "/" + objectName;
    }

    public String getFileUrl(String objectName) {
        return getFileUrl(defaultBucketName, objectName);
    }

    public String getBucketName() {
        return defaultBucketName;
    }

    public String getProductImagesBucket() {
        return productImagesBucket;
    }

    public String getProductVideosBucket() {
        return productVideosBucket;
    }
}
