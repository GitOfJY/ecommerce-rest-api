package com.jy.shoppy.domain.prodcut.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "상품 수정 요청")
public class UpdateProductRequest {

    @Schema(description = "상품명", example = "반팔 티셔츠")
    private String name;

    @Schema(description = "상품 설명", example = "100% 면 소재")
    private String description;

    @Schema(description = "가격", example = "29900")
    private BigDecimal price;

    @Schema(description = "카테고리 ID 목록")
    private List<Long> categoryIds;

    @Schema(description = "상품 옵션 목록")
    private List<UpdateProductOptionRequest> options;

    @Schema(description = "상품 이미지 URL 목록 (첫 번째가 대표 이미지)")
    private List<String> imageUrls;
}