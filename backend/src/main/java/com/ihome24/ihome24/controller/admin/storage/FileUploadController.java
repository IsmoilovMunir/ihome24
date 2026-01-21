package com.ihome24.ihome24.controller.admin.storage;

import com.ihome24.ihome24.config.security.UserPrincipal;
import com.ihome24.ihome24.dto.response.storage.FileUploadResponse;
import com.ihome24.ihome24.entity.storage.FileMetadata;
import com.ihome24.ihome24.service.storage.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/files")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FileUploadController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "sortOrder", required = false) Integer sortOrder,
            Authentication authentication) {

        Long userId = getUserId(authentication);

        FileUploadResponse response = fileService.uploadFile(file, productId, userId, sortOrder);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/upload/multiple")
    public ResponseEntity<List<FileUploadResponse>> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "productId", required = false) Long productId,
            Authentication authentication) {

        Long userId = getUserId(authentication);

        List<FileUploadResponse> responses = new java.util.ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            // Используем индекс как sortOrder для множественной загрузки
            FileUploadResponse response = fileService.uploadFile(file, productId, userId, i);
            responses.add(response);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable Long fileId,
            Authentication authentication) {

        Long userId = getUserId(authentication);
        fileService.deleteFile(fileId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<FileUploadResponse>> getProductFiles(@PathVariable Long productId) {
        List<FileUploadResponse> files = fileService.getProductFiles(productId);
        return ResponseEntity.ok(files);
    }

    @GetMapping("/product/{productId}/images")
    public ResponseEntity<List<FileUploadResponse>> getProductImages(@PathVariable Long productId) {
        List<FileUploadResponse> files = fileService.getProductFilesByType(productId, FileMetadata.FileType.IMAGE);
        return ResponseEntity.ok(files);
    }

    @GetMapping("/product/{productId}/videos")
    public ResponseEntity<List<FileUploadResponse>> getProductVideos(@PathVariable Long productId) {
        List<FileUploadResponse> files = fileService.getProductFilesByType(productId, FileMetadata.FileType.VIDEO);
        return ResponseEntity.ok(files);
    }

    @PutMapping("/sort")
    public ResponseEntity<Void> updateFilesSortOrder(@RequestBody List<Long> fileIds) {
        fileService.updateFilesSortOrder(fileIds);
        return ResponseEntity.ok().build();
    }


    private Long getUserId(Authentication authentication) {
        // Временно для разработки: если нет аутентификации, используем ID 1 (админ)
        if (authentication == null || authentication.getPrincipal() == null) {
            log.warn("No authentication found, using default admin ID for development");
            return 1L; // Временно используем ID администратора
        }

        if (authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }

        // Fallback для разработки
        log.warn("Unable to determine user ID from authentication, using default admin ID");
        return 1L;
    }
}
