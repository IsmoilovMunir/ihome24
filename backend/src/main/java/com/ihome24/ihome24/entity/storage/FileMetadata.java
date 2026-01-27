package com.ihome24.ihome24.entity.storage;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_metadata")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "thumbnail_path", length = 500)
    private String thumbnailPath;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "file_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private FileType fileType;

    @Column(name = "media_size", length = 20)
    @Enumerated(EnumType.STRING)
    private MediaSize mediaSize;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "bucket_name", nullable = false, length = 100)
    private String bucketName;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "is_main")
    @Builder.Default
    private Boolean isMain = false;

    @Column(name = "file_group", length = 100)
    private String fileGroup;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum FileType {
        IMAGE,
        VIDEO
    }

    public enum MediaSize {
        ORIGINAL,
        SMALL,
        MEDIUM,
        LARGE
    }
}
