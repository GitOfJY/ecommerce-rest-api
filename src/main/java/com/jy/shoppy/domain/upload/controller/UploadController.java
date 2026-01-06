package com.jy.shoppy.domain.upload.controller;

import com.jy.shoppy.domain.upload.dto.ImageUploadResponse;
import com.jy.shoppy.domain.upload.service.ImageUploadService;
import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import com.jy.shoppy.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Tag(name = "Upload", description = "이미지 업로드 API")
public class UploadController {
    private final ImageUploadService imageUploadService;

    @PostMapping(value = "/products/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<ImageUploadResponse>>> uploadProductImages(
            @RequestParam("files") List<MultipartFile> files) {
        if (files.size() > 10) {
            throw new ServiceException(ServiceExceptionCode.FILE_UPLOAD_FAILED_PRODUCT_SIZE);
        }
        List<String> imageUrls = imageUploadService.uploadImages(files, "products");
        List<ImageUploadResponse> responses = imageUrls.stream()
                .map(ImageUploadResponse::new)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responses, HttpStatus.OK));
    }

    @PostMapping(value = "/reviews/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<ImageUploadResponse>>> uploadReviewImages(
            @RequestParam("files") List<MultipartFile> files) {
        if (files.size() > 5) {
            throw new ServiceException(ServiceExceptionCode.FILE_UPLOAD_FAILED_REVIEW_SIZE);
        }
        List<String> imageUrls = imageUploadService.uploadImages(files, "reviews");
        List<ImageUploadResponse> responses = imageUrls.stream()
                .map(ImageUploadResponse::new)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses, HttpStatus.OK));
    }
}