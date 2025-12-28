package com.jy.shoppy.domain.prodcut.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
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
@Schema(description = "상품 등록 요청")
public class CreateProductRequest {
    @NotBlank(message = "상품명은 필수입니다.")
    @Size(max = 50, message = "상품명은 50자 이하로 입력해주세요.")
    @Schema(description = "상품명", example = "반팔 티셔츠")
    private String name;

    @NotBlank
    @Schema(description = "상품 설명", example = "100% 면 소재의 편안한 티셔츠입니다")
    private String description;

    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    @Schema(description = "가격", example = "29900")
    private BigDecimal price;

    @Valid
    @NotEmpty(message = "옵션은 최소 1개 이상 등록해야 합니다.")
    @Schema(description = "상품 옵션 목록")
    private List<ProductOptionRequest> options;

    @NotEmpty
    @Schema(description = "카테고리 ID 목록", example = "[1, 2]")
    private List<Long> categoryIds;

    @Schema(description = "상품 이미지 URL 목록 (첫 번째가 대표 이미지)",
            example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
    @Size(max = 10, message = "이미지는 최대 10개까지 등록 가능합니다")
    private List<String> imageUrls;

    public boolean hasOptions() {
        if (options.size() == 1) {
            ProductOptionRequest first = options.get(0);
            return first.getColor() != null || first.getSize() != null;
        }
        return true;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "상품 옵션")
    public static class ProductOptionRequest {
        @Schema(description = "색상", example = "블랙")
        private String color;

        @Schema(description = "사이즈", example = "L")
        private String size;

        @NotNull(message = "재고는 필수입니다")
        @Min(value = 0, message = "재고는 0 이상이어야 합니다")
        @Schema(description = "재고", example = "100")
        private Integer stock;

        @Schema(description = "추가 가격", example = "2000")
        private BigDecimal additionalPrice;
    }
}
