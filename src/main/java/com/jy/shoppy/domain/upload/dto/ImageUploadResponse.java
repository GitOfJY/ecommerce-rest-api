package com.jy.shoppy.domain.upload.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "이미지 업로드 응답")
public class ImageUploadResponse {
    @Schema(description = "업로드된 이미지 URL", example = "http://localhost:8080/uploads/products/abc123.jpg")
    private String imageUrl;
}
