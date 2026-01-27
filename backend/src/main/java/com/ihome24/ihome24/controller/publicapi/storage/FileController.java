package com.ihome24.ihome24.controller.publicapi.storage;

import com.ihome24.ihome24.service.storage.FileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FileController {

    private final FileService fileService;

    @GetMapping(value = "/**", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getFile(HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        String prefix = "/api/files";
        String filePath = requestPath.startsWith(prefix)
                ? requestPath.substring(prefix.length())
                : requestPath;

        if (filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }

        if (filePath.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            String[] parts = filePath.split("/", 2);
            String bucketName = parts.length > 1 ? parts[0] : null;
            String objectName = parts.length > 1 ? parts[1] : parts[0];

            InputStream inputStream;
            if (bucketName != null && parts.length > 1) {
                try {
                    inputStream = fileService.getFile(bucketName, objectName);
                } catch (RuntimeException e) {
                    inputStream = fileService.getFile(filePath);
                }
            } else {
                inputStream = fileService.getFile(objectName);
            }

            byte[] fileData = inputStream.readAllBytes();
            inputStream.close();

            String contentType = determineContentType(objectName);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(fileData.length);
            headers.setCacheControl("public, max-age=31536000"); // Кэширование на 1 год

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileData);
        } catch (Exception e) {
            log.error("Error retrieving file {}: {}", filePath, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    private String determineContentType(String filePath) {
        String lowerPath = filePath.toLowerCase();
        if (lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerPath.endsWith(".png")) {
            return "image/png";
        } else if (lowerPath.endsWith(".webp")) {
            return "image/webp";
        } else if (lowerPath.endsWith(".mp4")) {
            return "video/mp4";
        } else if (lowerPath.endsWith(".webm")) {
            return "video/webm";
        } else if (lowerPath.endsWith(".avi")) {
            return "video/x-msvideo";
        }
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}
