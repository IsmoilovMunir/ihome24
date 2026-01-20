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
    
    Optional<FileMetadata> findByFilePath(String filePath);
    
    List<FileMetadata> findByUserId(Long userId);
}
