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

import jakarta.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final MinIOService minIOService;
    private final FileMetadataRepository fileMetadataRepository;

    @Value("${minio.max-image-size:10485760}")
    private long maxImageSize;

    @Value("${minio.max-video-size:104857600}")
    private long maxVideoSize;

    @Value("${minio.image-size-small:300}")
    private int imageSmallWidth;

    @Value("${minio.image-size-medium:600}")
    private int imageMediumWidth;

    @Value("${minio.image-size-large:1200}")
    private int imageLargeWidth;

    @Value("${minio.image-quality-small:0.7}")
    private float imageSmallQuality;

    @Value("${minio.image-quality-medium:0.75}")
    private float imageMediumQuality;

    @Value("${minio.image-quality-large:0.8}")
    private float imageLargeQuality;

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/png", "image/jpeg", "image/jpg", "image/webp"
    );

    private static final Set<String> ALLOWED_VIDEO_TYPES = Set.of(
            "video/mp4", "video/webm"
    );

    private static final Pattern IMAGE_EXTENSIONS = Pattern.compile(
            ".*\\.(png|jpg|jpeg|webp)$", Pattern.CASE_INSENSITIVE
    );

    private static final Pattern VIDEO_EXTENSIONS = Pattern.compile(
            ".*\\.(mp4|webm)$", Pattern.CASE_INSENSITIVE
    );

    @PostConstruct
    public void initializeImageIoPlugins() {
        ImageIO.scanForPlugins();
        if (!ImageIO.getImageWritersByFormatName("webp").hasNext()) {
            log.warn("WebP ImageIO plugin not found. Falling back to JPEG for resized variants.");
        }
    }

    @Transactional
    public FileUploadResponse uploadFile(MultipartFile file, Long productId, Long userId, Integer sortOrder) {
        validateFile(file);

        try {
            FileMetadata.FileType fileType = determineFileType(file.getContentType(), file.getOriginalFilename());
            if (fileType == FileMetadata.FileType.IMAGE) {
                return uploadImage(file, productId, userId, sortOrder);
            }
            return uploadVideo(file, productId, userId, sortOrder);
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

        try {
            if (metadata.getFileGroup() != null) {
                List<FileMetadata> groupFiles = fileMetadataRepository.findByFileGroupAndProductIdAndFileType(
                        metadata.getFileGroup(),
                        metadata.getProductId(),
                        metadata.getFileType()
                );
                for (FileMetadata groupFile : groupFiles) {
                    minIOService.deleteFile(groupFile.getBucketName(), groupFile.getFilePath());
                }
                fileMetadataRepository.deleteAll(groupFiles);
            } else {
                minIOService.deleteFile(metadata.getBucketName(), metadata.getFilePath());
                fileMetadataRepository.delete(metadata);
            }

            log.info("File deleted successfully - userId: {}, fileId: {}, fileName: {}, timestamp: {}",
                    userId, fileId, metadata.getFileName(), LocalDateTime.now());

        } catch (Exception e) {
            log.error("Error deleting file - userId: {}, fileId: {}, error: {}",
                    userId, fileId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    public InputStream getFile(String bucketName, String objectName) {
        return minIOService.getFile(bucketName, objectName);
    }

    public InputStream getFile(String objectName) {
        return minIOService.getFile(objectName);
    }

    public List<FileUploadResponse> getProductFiles(Long productId) {
        List<FileMetadata> files = new ArrayList<>();
        files.addAll(fileMetadataRepository.findByProductIdAndFileTypeAndMediaSize(
                productId, FileMetadata.FileType.IMAGE, FileMetadata.MediaSize.MEDIUM));
        files.addAll(fileMetadataRepository.findByProductIdAndFileTypeAndMediaSize(
                productId, FileMetadata.FileType.VIDEO, FileMetadata.MediaSize.ORIGINAL));
        files.sort((a, b) -> Integer.compare(
                a.getSortOrder() != null ? a.getSortOrder() : 0,
                b.getSortOrder() != null ? b.getSortOrder() : 0
        ));
        return files.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<FileUploadResponse> getProductFilesByType(Long productId, FileMetadata.FileType fileType) {
        FileMetadata.MediaSize size = fileType == FileMetadata.FileType.IMAGE
                ? FileMetadata.MediaSize.MEDIUM
                : FileMetadata.MediaSize.ORIGINAL;
        List<FileMetadata> files = fileMetadataRepository.findByProductIdAndFileTypeAndMediaSize(productId, fileType, size);
        files.sort((a, b) -> Integer.compare(
                a.getSortOrder() != null ? a.getSortOrder() : 0,
                b.getSortOrder() != null ? b.getSortOrder() : 0
        ));
        return files.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public void updateFileSortOrder(Long fileId, Integer sortOrder) {
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found: " + fileId));
        updateGroupSortAndMain(metadata, sortOrder, metadata.getIsMain() != null && metadata.getIsMain());
    }

    @Transactional
    public void updateFilesSortOrder(List<Long> fileIds) {
        for (int i = 0; i < fileIds.size(); i++) {
            Long fileId = fileIds.get(i);
            FileMetadata metadata = fileMetadataRepository.findById(fileId)
                    .orElseThrow(() -> new IllegalArgumentException("File not found: " + fileId));
            boolean isMain = metadata.getFileType() == FileMetadata.FileType.IMAGE && i == 0;
            updateGroupSortAndMain(metadata, i, isMain);
        }
    }

    @Transactional
    public void deleteProductFiles(Long productId) {
        List<FileMetadata> files = fileMetadataRepository.findByProductId(productId);
        for (FileMetadata metadata : files) {
            minIOService.deleteFile(metadata.getBucketName(), metadata.getFilePath());
        }
        fileMetadataRepository.deleteAll(files);
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

        boolean isImage = ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase(Locale.ROOT)) ||
                IMAGE_EXTENSIONS.matcher(originalFilename).matches();
        boolean isVideo = ALLOWED_VIDEO_TYPES.contains(contentType.toLowerCase(Locale.ROOT)) ||
                VIDEO_EXTENSIONS.matcher(originalFilename).matches();

        if (!isImage && !isVideo) {
            throw new IllegalArgumentException(
                    "Unsupported file type. Allowed: JPG, JPEG, PNG, WEBP (images), MP4, WEBM (videos)");
        }

        long maxSize = isImage ? maxImageSize : maxVideoSize;
        if (file.getSize() > maxSize) {
            String maxSizeMB = String.format("%.2f", maxSize / (1024.0 * 1024.0));
            throw new IllegalArgumentException(
                    String.format("File size exceeds maximum allowed size: %s MB", maxSizeMB));
        }
    }

    private FileMetadata.FileType determineFileType(String contentType, String filename) {
        if (contentType != null) {
            if (ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
                return FileMetadata.FileType.IMAGE;
            }
            if (ALLOWED_VIDEO_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
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

    private FileUploadResponse uploadImage(MultipartFile file, Long productId, Long userId, Integer sortOrder) throws IOException {
        String originalName = file.getOriginalFilename();
        String extension = normalizeExtension(getFileExtension(originalName), file.getContentType());
        String fileGroup = generateFileGroup(originalName);
        String baseName = fileGroup;
        String productFolder = buildProductFolder(productId, userId);
        String bucketName = minIOService.getProductImagesBucket();

        byte[] originalBytes = file.getBytes();
        String originalObjectName = productFolder + "/original/" + baseName + extension;
        minIOService.uploadFile(bucketName, originalObjectName, new ByteArrayInputStream(originalBytes), file.getContentType(), originalBytes.length);

        String variantFormat = getVariantFormat(file.getContentType(), extension);
        String variantExtension = "." + variantFormat;
        String variantContentType = resolveImageContentType(variantFormat);

        byte[] smallBytes = resizeToFormat(originalBytes, imageSmallWidth, imageSmallQuality, variantFormat);
        byte[] mediumBytes = resizeToFormat(originalBytes, imageMediumWidth, imageMediumQuality, variantFormat);
        byte[] largeBytes = resizeToFormat(originalBytes, imageLargeWidth, imageLargeQuality, variantFormat);

        String smallObjectName = productFolder + "/small/" + baseName + variantExtension;
        String mediumObjectName = productFolder + "/medium/" + baseName + variantExtension;
        String largeObjectName = productFolder + "/large/" + baseName + variantExtension;

        minIOService.uploadFile(bucketName, smallObjectName, smallBytes, variantContentType);
        minIOService.uploadFile(bucketName, mediumObjectName, mediumBytes, variantContentType);
        minIOService.uploadFile(bucketName, largeObjectName, largeBytes, variantContentType);

        int resolvedSortOrder = resolveSortOrder(productId, sortOrder, FileMetadata.FileType.IMAGE);
        boolean isMain = isMainImage(productId, resolvedSortOrder);

        List<FileMetadata> metadataList = List.of(
                buildMetadata(file, baseName + extension, originalObjectName, bucketName, originalBytes.length,
                        FileMetadata.FileType.IMAGE, FileMetadata.MediaSize.ORIGINAL, productId, userId, resolvedSortOrder, isMain, fileGroup, null),
                buildMetadata(file, baseName + variantExtension, smallObjectName, bucketName, smallBytes.length,
                        FileMetadata.FileType.IMAGE, FileMetadata.MediaSize.SMALL, productId, userId, resolvedSortOrder, isMain, fileGroup, variantContentType),
                buildMetadata(file, baseName + variantExtension, mediumObjectName, bucketName, mediumBytes.length,
                        FileMetadata.FileType.IMAGE, FileMetadata.MediaSize.MEDIUM, productId, userId, resolvedSortOrder, isMain, fileGroup, variantContentType),
                buildMetadata(file, baseName + variantExtension, largeObjectName, bucketName, largeBytes.length,
                        FileMetadata.FileType.IMAGE, FileMetadata.MediaSize.LARGE, productId, userId, resolvedSortOrder, isMain, fileGroup, variantContentType)
        );

        fileMetadataRepository.saveAll(metadataList);

        FileMetadata mediumMetadata = metadataList.stream()
                .filter(meta -> meta.getMediaSize() == FileMetadata.MediaSize.MEDIUM)
                .findFirst()
                .orElse(metadataList.get(0));

        FileMetadata smallMetadata = metadataList.stream()
                .filter(meta -> meta.getMediaSize() == FileMetadata.MediaSize.SMALL)
                .findFirst()
                .orElse(null);

        log.info("Image uploaded successfully - userId: {}, productId: {}, fileGroup: {}, size: {} bytes, timestamp: {}",
                userId, productId, fileGroup, file.getSize(), LocalDateTime.now());

        return mapToResponseWithThumbnail(mediumMetadata, smallMetadata);
    }

    private FileUploadResponse uploadVideo(MultipartFile file, Long productId, Long userId, Integer sortOrder) throws IOException {
        String originalName = file.getOriginalFilename();
        String extension = normalizeExtension(getFileExtension(originalName), file.getContentType());
        String fileGroup = generateFileGroup(originalName);
        String baseName = fileGroup;
        String productFolder = buildProductFolder(productId, userId);
        String bucketName = minIOService.getProductVideosBucket();

        String objectName = productFolder + "/original/" + baseName + extension;
        minIOService.uploadFile(bucketName, objectName, file.getInputStream(), file.getContentType(), file.getSize());

        int resolvedSortOrder = resolveSortOrder(productId, sortOrder, FileMetadata.FileType.VIDEO);

        FileMetadata metadata = buildMetadata(file, baseName + extension, objectName, bucketName, file.getSize(),
                FileMetadata.FileType.VIDEO, FileMetadata.MediaSize.ORIGINAL, productId, userId, resolvedSortOrder, false, fileGroup, null);
        FileMetadata savedMetadata = fileMetadataRepository.save(metadata);

        log.info("Video uploaded successfully - userId: {}, productId: {}, fileId: {}, fileName: {}, size: {} bytes, timestamp: {}",
                userId, productId, savedMetadata.getId(), savedMetadata.getFileName(), file.getSize(), LocalDateTime.now());

        return mapToResponse(savedMetadata);
    }

    private FileMetadata buildMetadata(MultipartFile file,
                                       String fileName,
                                       String filePath,
                                       String bucketName,
                                       long size,
                                       FileMetadata.FileType fileType,
                                       FileMetadata.MediaSize mediaSize,
                                       Long productId,
                                       Long userId,
                                       Integer sortOrder,
                                       boolean isMain,
                                       String fileGroup,
                                       String overrideContentType) {
        return FileMetadata.builder()
                .fileName(fileName)
                .originalName(file.getOriginalFilename())
                .filePath(filePath)
                .contentType(overrideContentType != null ? overrideContentType : file.getContentType())
                .fileSize(size)
                .fileType(fileType)
                .mediaSize(mediaSize)
                .productId(productId)
                .userId(userId)
                .bucketName(bucketName)
                .sortOrder(sortOrder)
                .isMain(isMain)
                .fileGroup(fileGroup)
                .build();
    }

    private String buildProductFolder(Long productId, Long userId) {
        if (productId != null) {
            return "products/" + productId;
        }
        return "products/draft/user-" + userId;
    }

    private int resolveSortOrder(Long productId, Integer sortOrder, FileMetadata.FileType fileType) {
        if (sortOrder != null) {
            return sortOrder;
        }
        if (productId == null) {
            return 0;
        }
        FileMetadata.MediaSize size = fileType == FileMetadata.FileType.IMAGE
                ? FileMetadata.MediaSize.MEDIUM
                : FileMetadata.MediaSize.ORIGINAL;
        List<FileMetadata> existingFiles = fileMetadataRepository.findByProductIdAndFileTypeAndMediaSize(
                productId, fileType, size);
        return existingFiles.stream()
                .map(f -> f.getSortOrder() != null ? f.getSortOrder() : 0)
                .max(Integer::compareTo)
                .orElse(-1) + 1;
    }

    private boolean isMainImage(Long productId, Integer sortOrder) {
        if (productId == null || sortOrder == null || sortOrder != 0) {
            return false;
        }
        Optional<FileMetadata> existingMain = fileMetadataRepository.findFirstByProductIdAndFileTypeAndIsMainTrue(
                productId, FileMetadata.FileType.IMAGE);
        return existingMain.isEmpty();
    }

    private byte[] resizeToFormat(byte[] originalBytes, int width, float quality, String format) throws IOException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(originalBytes);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Thumbnails.Builder<? extends InputStream> builder = Thumbnails.of(inputStream)
                    .size(width, width)
                    .outputFormat(format);
            if (isLossyFormat(format)) {
                builder.outputQuality(quality);
            }
            builder.toOutputStream(outputStream);
            return outputStream.toByteArray();
        }
    }

    private String getVariantFormat(String contentType, String extension) {
        String normalized = extension != null ? extension.toLowerCase(Locale.ROOT) : "";
        if (normalized.startsWith(".")) {
            normalized = normalized.substring(1);
        }
        String format = switch (normalized) {
            case "png" -> "png";
            case "jpg", "jpeg" -> "jpg";
            case "webp" -> "webp";
            default -> {
                if (contentType != null) {
                    String type = contentType.toLowerCase(Locale.ROOT);
                    if (type.contains("png")) {
                        yield "png";
                    }
                    if (type.contains("jpeg") || type.contains("jpg")) {
                        yield "jpg";
                    }
                    if (type.contains("webp")) {
                        yield "webp";
                    }
                }
                yield "jpg";
            }
        };
        if ("webp".equals(format) && !ImageIO.getImageWritersByFormatName("webp").hasNext()) {
            log.warn("WebP plugin not available, falling back to JPEG variants.");
            return "jpg";
        }
        return format;
    }

    private boolean isLossyFormat(String format) {
        return "jpg".equalsIgnoreCase(format) || "jpeg".equalsIgnoreCase(format) || "webp".equalsIgnoreCase(format);
    }

    private String resolveImageContentType(String format) {
        if ("png".equalsIgnoreCase(format)) {
            return "image/png";
        }
        if ("webp".equalsIgnoreCase(format)) {
            return "image/webp";
        }
        return "image/jpeg";
    }

    private void updateGroupSortAndMain(FileMetadata metadata, int sortOrder, boolean isMain) {
        if (metadata.getFileGroup() == null) {
            metadata.setSortOrder(sortOrder);
            metadata.setIsMain(isMain);
            fileMetadataRepository.save(metadata);
            return;
        }

        List<FileMetadata> groupFiles = fileMetadataRepository.findByFileGroupAndProductIdAndFileType(
                metadata.getFileGroup(),
                metadata.getProductId(),
                metadata.getFileType()
        );
        for (FileMetadata groupFile : groupFiles) {
            groupFile.setSortOrder(sortOrder);
            groupFile.setIsMain(isMain);
        }
        fileMetadataRepository.saveAll(groupFiles);
    }

    private String generateFileGroup(String originalFilename) {
        String base = sanitizeBaseName(originalFilename);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%s_%s_%s", base, timestamp, uuid);
    }

    private String sanitizeBaseName(String filename) {
        if (filename == null || filename.isBlank()) {
            return "file";
        }
        String name = filename;
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            name = filename.substring(0, lastDot);
        }
        name = name.replaceAll("[^a-zA-Z0-9-_]+", "-");
        name = name.replaceAll("-{2,}", "-");
        name = name.replaceAll("^-|-$", "");
        return name.isBlank() ? "file" : name.toLowerCase(Locale.ROOT);
    }

    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot) : "";
    }

    private String normalizeExtension(String extension, String contentType) {
        if (extension != null && !extension.isBlank()) {
            return extension.toLowerCase(Locale.ROOT);
        }
        if (contentType == null) {
            return "";
        }
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "video/mp4" -> ".mp4";
            case "video/webm" -> ".webm";
            default -> "";
        };
    }

    private FileUploadResponse mapToResponse(FileMetadata metadata) {
        FileMetadata thumbnailMetadata = null;
        if (metadata.getFileType() == FileMetadata.FileType.IMAGE
                && metadata.getMediaSize() == FileMetadata.MediaSize.MEDIUM
                && metadata.getFileGroup() != null) {
            thumbnailMetadata = fileMetadataRepository.findFirstByFileGroupAndProductIdAndFileTypeAndMediaSize(
                    metadata.getFileGroup(),
                    metadata.getProductId(),
                    metadata.getFileType(),
                    FileMetadata.MediaSize.SMALL
            ).orElse(null);
        }
        return mapToResponseWithThumbnail(metadata, thumbnailMetadata);
    }

    private FileUploadResponse mapToResponseWithThumbnail(FileMetadata metadata, FileMetadata thumbnailMetadata) {
        return FileUploadResponse.builder()
                .id(metadata.getId())
                .fileName(metadata.getFileName())
                .originalName(metadata.getOriginalName())
                .filePath(metadata.getFilePath())
                .thumbnailPath(thumbnailMetadata != null ? thumbnailMetadata.getFilePath() : null)
                .contentType(metadata.getContentType())
                .fileSize(metadata.getFileSize())
                .fileType(metadata.getFileType().name())
                .mediaSize(metadata.getMediaSize() != null ? metadata.getMediaSize().name() : null)
                .url(minIOService.getFileUrl(metadata.getBucketName(), metadata.getFilePath()))
                .thumbnailUrl(thumbnailMetadata != null
                        ? minIOService.getFileUrl(thumbnailMetadata.getBucketName(), thumbnailMetadata.getFilePath())
                        : null)
                .isMain(metadata.getIsMain())
                .createdAt(metadata.getCreatedAt())
                .sortOrder(metadata.getSortOrder())
                .build();
    }
}
