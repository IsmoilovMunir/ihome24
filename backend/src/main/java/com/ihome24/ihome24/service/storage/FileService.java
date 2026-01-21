package com.ihome24.ihome24.service.storage;

import com.ihome24.ihome24.dto.response.storage.FileUploadResponse;
import com.ihome24.ihome24.entity.storage.FileMetadata;
import com.ihome24.ihome24.repository.storage.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final MinIOService minIOService;
    private final FileMetadataRepository fileMetadataRepository;

    @Value("${minio.max-image-size:5242880}")
    private long maxImageSize;

    @Value("${minio.max-video-size:104857600}")
    private long maxVideoSize;

    @Value("${minio.thumbnail-width:300}")
    private int thumbnailWidth;

    @Value("${minio.thumbnail-height:300}")
    private int thumbnailHeight;

    // Поддерживаемые форматы фото (PNG приоритетный)
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/png", "image/jpeg", "image/jpg", "image/webp"
    );

    // Поддерживаемые форматы видео
    private static final Set<String> ALLOWED_VIDEO_TYPES = Set.of(
            "video/mp4", "video/webm", "video/x-msvideo" // AVI
    );

    // Расширения файлов для фото (PNG приоритетный)
    private static final Pattern IMAGE_EXTENSIONS = Pattern.compile(
            ".*\\.(png|jpg|jpeg|webp)$", Pattern.CASE_INSENSITIVE
    );

    // Расширения файлов для видео
    private static final Pattern VIDEO_EXTENSIONS = Pattern.compile(
            ".*\\.(mp4|webm|avi)$", Pattern.CASE_INSENSITIVE
    );

    @Transactional
    public FileUploadResponse uploadFile(MultipartFile file, Long productId, Long userId, Integer sortOrder) {
        // Валидация файла
        validateFile(file);

        try {
            // Определяем тип файла
            FileMetadata.FileType fileType = determineFileType(file.getContentType(), file.getOriginalFilename());
            
            // Генерируем уникальное имя файла
            String fileName = generateFileName(file.getOriginalFilename(), fileType);
            String filePath = generateFilePath(fileType, fileName);

            // Загружаем файл в MinIO
            minIOService.uploadFile(filePath, file.getInputStream(), file.getContentType(), file.getSize());

            // Генерируем превью для фото
            String thumbnailPath = null;
            if (fileType == FileMetadata.FileType.IMAGE) {
                thumbnailPath = generateThumbnail(file, filePath);
            }

            // Определяем sortOrder - если не указан, берем максимальный + 1 для данного товара
            if (sortOrder == null && productId != null) {
                List<FileMetadata> existingFiles = fileMetadataRepository.findByProductId(productId);
                sortOrder = existingFiles.stream()
                        .map(f -> f.getSortOrder() != null ? f.getSortOrder() : 0)
                        .max(Integer::compareTo)
                        .orElse(-1) + 1;
            } else if (sortOrder == null) {
                sortOrder = 0;
            }

            // Сохраняем метаданные в БД
            FileMetadata metadata = FileMetadata.builder()
                    .fileName(fileName)
                    .originalName(file.getOriginalFilename())
                    .filePath(filePath)
                    .thumbnailPath(thumbnailPath)
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .fileType(fileType)
                    .productId(productId)
                    .userId(userId)
                    .bucketName(minIOService.getBucketName())
                    .sortOrder(sortOrder)
                    .build();

            FileMetadata savedMetadata = fileMetadataRepository.save(metadata);

            // Логирование
            log.info("File uploaded successfully - userId: {}, productId: {}, fileId: {}, fileName: {}, size: {} bytes, timestamp: {}",
                    userId, productId, savedMetadata.getId(), fileName, file.getSize(), LocalDateTime.now());

            return FileUploadResponse.builder()
                    .id(savedMetadata.getId())
                    .fileName(savedMetadata.getFileName())
                    .originalName(savedMetadata.getOriginalName())
                    .filePath(savedMetadata.getFilePath())
                    .thumbnailPath(savedMetadata.getThumbnailPath())
                    .contentType(savedMetadata.getContentType())
                    .fileSize(savedMetadata.getFileSize())
                    .fileType(savedMetadata.getFileType().name())
                    .url(minIOService.getFileUrl(savedMetadata.getFilePath()))
                    .thumbnailUrl(savedMetadata.getThumbnailPath() != null ?
                            minIOService.getFileUrl(savedMetadata.getThumbnailPath()) : null)
                    .sortOrder(savedMetadata.getSortOrder())
                    .build();

        } catch (IOException e) {
            log.error("Error uploading file - userId: {}, productId: {}, error: {}",
                    userId, productId, e.getMessage(), e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    @Transactional
    public void deleteFile(Long fileId, Long userId) {
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found: " + fileId));

        // Проверяем права доступа (только администратор может удалять)
        // Это будет проверяться в контроллере через Spring Security

        try {
            // Удаляем файл из MinIO
            minIOService.deleteFile(metadata.getFilePath());

            // Удаляем превью, если есть
            if (metadata.getThumbnailPath() != null) {
                minIOService.deleteFile(metadata.getThumbnailPath());
            }

            // Удаляем метаданные из БД
            fileMetadataRepository.delete(metadata);

            // Логирование
            log.info("File deleted successfully - userId: {}, fileId: {}, fileName: {}, timestamp: {}",
                    userId, fileId, metadata.getFileName(), LocalDateTime.now());

        } catch (Exception e) {
            log.error("Error deleting file - userId: {}, fileId: {}, error: {}",
                    userId, fileId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    public InputStream getFile(String filePath) {
        return minIOService.getFile(filePath);
    }

    public List<FileUploadResponse> getProductFiles(Long productId) {
        List<FileMetadata> files = fileMetadataRepository.findByProductId(productId);
        // Сортируем по sortOrder
        files.sort((a, b) -> Integer.compare(
                a.getSortOrder() != null ? a.getSortOrder() : 0,
                b.getSortOrder() != null ? b.getSortOrder() : 0
        ));
        return files.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<FileUploadResponse> getProductFilesByType(Long productId, FileMetadata.FileType fileType) {
        List<FileMetadata> files = fileMetadataRepository.findByProductIdAndFileType(productId, fileType);
        // Сортируем по sortOrder
        files.sort((a, b) -> Integer.compare(
                a.getSortOrder() != null ? a.getSortOrder() : 0,
                b.getSortOrder() != null ? b.getSortOrder() : 0
        ));
        return files.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or null");
        }

        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();

        if (contentType == null || originalFilename == null) {
            throw new IllegalArgumentException("File content type or name is missing");
        }

        // Проверяем тип файла
        boolean isImage = ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase()) ||
                IMAGE_EXTENSIONS.matcher(originalFilename).matches();
        boolean isVideo = ALLOWED_VIDEO_TYPES.contains(contentType.toLowerCase()) ||
                VIDEO_EXTENSIONS.matcher(originalFilename).matches();

        if (!isImage && !isVideo) {
            throw new IllegalArgumentException(
                    "Unsupported file type. Allowed: JPG, JPEG, PNG, WEBP (images), MP4, WEBM, AVI (videos)");
        }

        // Проверяем размер файла
        long maxSize = isImage ? maxImageSize : maxVideoSize;
        if (file.getSize() > maxSize) {
            String maxSizeMB = String.format("%.2f", maxSize / (1024.0 * 1024.0));
            throw new IllegalArgumentException(
                    String.format("File size exceeds maximum allowed size: %s MB", maxSizeMB));
        }
    }

    private FileMetadata.FileType determineFileType(String contentType, String filename) {
        if (contentType != null) {
            if (ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
                return FileMetadata.FileType.IMAGE;
            }
            if (ALLOWED_VIDEO_TYPES.contains(contentType.toLowerCase())) {
                return FileMetadata.FileType.VIDEO;
            }
        }

        if (filename != null) {
            if (IMAGE_EXTENSIONS.matcher(filename).matches()) {
                return FileMetadata.FileType.IMAGE;
            }
            if (VIDEO_EXTENSIONS.matcher(filename).matches()) {
                return FileMetadata.FileType.VIDEO;
            }
        }

        throw new IllegalArgumentException("Cannot determine file type");
    }

    private String generateFileName(String originalFilename, FileMetadata.FileType fileType) {
        String extension = getFileExtension(originalFilename);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%s_%s_%s%s", fileType.name().toLowerCase(), timestamp, uuid, extension);
    }

    private String generateFilePath(FileMetadata.FileType fileType, String fileName) {
        String folder = fileType == FileMetadata.FileType.IMAGE ? "images" : "videos";
        String yearMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        return String.format("%s/%s/%s", folder, yearMonth, fileName);
    }

    private String generateThumbnail(MultipartFile file, String originalFilePath) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Thumbnails.of(inputStream)
                    .size(thumbnailWidth, thumbnailHeight)
                    .outputFormat("jpg")
                    .toOutputStream(outputStream);

            byte[] thumbnailData = outputStream.toByteArray();
            String thumbnailPath = originalFilePath.replace("/images/", "/thumbnails/")
                    .replaceAll("\\.(jpg|jpeg|png|webp)$", ".jpg");

            minIOService.uploadFile(thumbnailPath, thumbnailData, "image/jpeg");

            return thumbnailPath;
        }
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot) : "";
    }

    @Transactional
    public void updateFileSortOrder(Long fileId, Integer sortOrder) {
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found: " + fileId));
        metadata.setSortOrder(sortOrder);
        fileMetadataRepository.save(metadata);
    }

    @Transactional
    public void updateFilesSortOrder(List<Long> fileIds) {
        for (int i = 0; i < fileIds.size(); i++) {
            updateFileSortOrder(fileIds.get(i), i);
        }
    }

    private FileUploadResponse mapToResponse(FileMetadata metadata) {
        return FileUploadResponse.builder()
                .id(metadata.getId())
                .fileName(metadata.getFileName())
                .originalName(metadata.getOriginalName())
                .filePath(metadata.getFilePath())
                .thumbnailPath(metadata.getThumbnailPath())
                .contentType(metadata.getContentType())
                .fileSize(metadata.getFileSize())
                .fileType(metadata.getFileType().name())
                .url(minIOService.getFileUrl(metadata.getFilePath()))
                .thumbnailUrl(metadata.getThumbnailPath() != null ?
                        minIOService.getFileUrl(metadata.getThumbnailPath()) : null)
                .createdAt(metadata.getCreatedAt())
                .sortOrder(metadata.getSortOrder())
                .build();
    }
}
