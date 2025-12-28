package com.jy.shoppy.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "리뷰 작성 가능한 상품 응답")
public class ReviewableProductResponse {
    @Schema(description = "주문 상품 ID")
    private Long orderProductId;

    @Schema(description = "주문 ID")
    private Long orderId;

    @Schema(description = "상품 ID")
    private Long productId;

    @Schema(description = "상품명")
    private String productName;

    @Schema(description = "선택한 색상")
    private String selectedColor;

    @Schema(description = "선택한 사이즈")
    private String selectedSize;

    @Schema(description = "주문 가격")
    private BigDecimal orderPrice;

    @Schema(description = "주문 수량")
    private Integer quantity;

    @Schema(description = "주문 일자")
    private LocalDateTime orderDate;
}