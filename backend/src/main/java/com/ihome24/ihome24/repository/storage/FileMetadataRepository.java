package com.ihome24.ihome24.repository.storage;

import com.ihome24.ihome24.entity.storage.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    
    List<FileMetadata> findByProductId(Long productId);
    
    List<FileMetadata> findByProductIdAndFileType(Long productId, FileMetadata.FileType fileType);

    List<FileMetadata> findByProductIdAndFileTypeAndMediaSize(Long productId, FileMetadata.FileType fileType, FileMetadata.MediaSize mediaSize);
    
    Optional<FileMetadata> findByFilePath(String filePath);
    
    List<FileMetadata> findByUserId(Long userId);

    List<FileMetadata> findByFileGroupAndProductIdAndFileType(String fileGroup, Long productId, FileMetadata.FileType fileType);

    Optional<FileMetadata> findFirstByProductIdAndFileTypeAndIsMainTrue(Long productId, FileMetadata.FileType fileType);

    Optional<FileMetadata> findFirstByFileGroupAndProductIdAndFileTypeAndMediaSize(String fileGroup, Long productId, FileMetadata.FileType fileType, FileMetadata.MediaSize mediaSize);

    /** Для черновиков: файлы загружены с productId=null (путь products/draft/user-X/...) */
    Optional<FileMetadata> findFirstByFileGroupAndFileTypeAndMediaSizeAndProductIdIsNull(String fileGroup, FileMetadata.FileType fileType, FileMetadata.MediaSize mediaSize);
}
