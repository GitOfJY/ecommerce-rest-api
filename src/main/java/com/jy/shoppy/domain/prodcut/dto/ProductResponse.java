package com.jy.shoppy.domain.prodcut.dto;

import com.jy.shoppy.domain.prodcut.entity.type.StockStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "상품 응답")
public class ProductResponse {
    @Schema(description = "상품 ID")
    private Long id;

    @Schema(description = "상품명")
    private String name;

    @Schema(description = "상품 설명")
    private String description;

    @Schema(description = "가격")
    private BigDecimal price;

    @Schema(description = "평균 평점")
    private BigDecimal averageRating;

    @Schema(description = "리뷰 개수")
    private Integer reviewCount;

    @Schema(description = "대표 이미지 URL")
    private String thumbnailUrl;

    @Schema(description = "모든 이미지 URL 목록")
    private List<String> imageUrls;

//    @Schema(description = "상품 옵션 목록")
//    private List<ProductOptionResponse> options;

    @Schema(description = "카테고리 ID 목록")
    private List<Long> categoryIds;

    @Schema(description = "등록일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;

    @Schema(description = "재고 상태 (IN_STOCK, LOW_STOCK, OUT_OF_STOCK)", example = "IN_STOCK")
    private StockStatus stockStatus;

    // 총 재고 추가
    @Schema(description = "총 재고 수량", example = "100")
    private Integer totalStock;
}