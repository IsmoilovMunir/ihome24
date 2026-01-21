package com.ihome24.ihome24.config.storage;

import io.minio.MinioClient;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class MinIOConfig {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.max-image-size:10485760}")
    private long maxImageSize;

    @Value("${minio.max-video-size:104857600}")
    private long maxVideoSize;

    @Value("${minio.thumbnail-width:300}")
    private int thumbnailWidth;

    @Value("${minio.thumbnail-height:300}")
    private int thumbnailHeight;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
