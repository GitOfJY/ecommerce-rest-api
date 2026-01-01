package com.jy.shoppy.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "리뷰 작성 가능한 상품 정보")
public class ReviewableProductResponse {

    @Schema(description = "주문 상품 ID (리뷰 작성 시 필요)")
    private Long orderProductId;

    @Schema(description = "주문 ID")
    private Long orderId;

    @Schema(description = "주문 날짜")
    private LocalDateTime orderDate;

    @Schema(description = "상품 ID")
    private Long productId;

    @Schema(description = "상품명")
    private String productName;

    @Schema(description = "상품 가격")
    private BigDecimal productPrice;

    @Schema(description = "상품 썸네일 URL")
    private String thumbnailUrl;

    @Schema(description = "구매 옵션 (색상)")
    private String color;

    @Schema(description = "구매 옵션 (사이즈)")
    private String size;

    @Schema(description = "구매 수량")
    private Integer quantity;
}