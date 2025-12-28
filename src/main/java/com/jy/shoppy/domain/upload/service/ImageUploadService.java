package com.jy.shoppy.domain.upload.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageUploadService {
    /**
     * 단일 이미지 업로드
     */
    String uploadImage(MultipartFile file, String directory);

    /**
     * 다중 이미지 업로드
     */
    List<String> uploadImages(List<MultipartFile> files, String directory);

    /**
     * 이미지 삭제
     */
    void deleteImage(String imageUrl);

    /**
     * 다중 이미지 삭제
     */
    void deleteImages(List<String> imageUrls);
}
