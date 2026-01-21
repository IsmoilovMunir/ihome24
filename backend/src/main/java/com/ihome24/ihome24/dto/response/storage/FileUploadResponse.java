package com.ihome24.ihome24.dto.response.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    private Long id;
    private String fileName;
    private String originalName;
    private String filePath;
    private String thumbnailPath;
    private String contentType;
    private Long fileSize;
    private String fileType;
    private String url;
    private String thumbnailUrl;
    private LocalDateTime createdAt;
    private Integer sortOrder;
}
