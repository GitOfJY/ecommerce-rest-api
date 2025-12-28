package com.jy.shoppy.domain.upload.service;

import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@ConditionalOnProperty(name = "file.upload.type", havingValue = "local")
public class LocalImageUploadService implements ImageUploadService {
    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    @Value("${file.upload.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    public String uploadImage(MultipartFile file, String directory) {
        validateImageFile(file);

        String filename = generateFilename(file.getOriginalFilename());

        try {
            Path uploadDir = Paths.get(uploadPath, directory);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String imageUrl = String.format("%s/%s/%s/%s", baseUrl, uploadPath, directory, filename);

            log.info("[LOCAL] Image uploaded: {}", imageUrl);
            return imageUrl;

        } catch (IOException e) {
            log.error("Failed to upload image to local storage", e);
            throw new ServiceException(ServiceExceptionCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public List<String> uploadImages(List<MultipartFile> files, String directory) {
        return files.stream()
                .map(file -> uploadImage(file, directory))
                .toList();
    }

    @Override
    public void deleteImage(String imageUrl) {
        try {
            String filePath = imageUrl.replace(baseUrl + "/", "");
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
            log.info("[LOCAL] Image deleted: {}", imageUrl);
        } catch (IOException e) {
            log.error("Failed to delete image from local storage", e);
        }
    }

    @Override
    public void deleteImages(List<String> imageUrls) {
        imageUrls.forEach(this::deleteImage);
    }

    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ServiceException(ServiceExceptionCode.EMPTY_FILE);
        }

        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new ServiceException(ServiceExceptionCode.FILE_TOO_LARGE);
        }

        String extension = getExtension(file.getOriginalFilename());
        if (!isImageFile(extension)) {
            throw new ServiceException(ServiceExceptionCode.INVALID_FILE_TYPE);
        }
    }

    private String generateFilename(String originalFilename) {
        String extension = getExtension(originalFilename);
        return UUID.randomUUID().toString() + "." + extension;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private boolean isImageFile(String extension) {
        return List.of("jpg", "jpeg", "png", "gif", "webp").contains(extension);
    }
}