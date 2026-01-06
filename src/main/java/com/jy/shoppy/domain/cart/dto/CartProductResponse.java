package com.jy.shoppy.domain.cart.dto;

import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "장바구니 상품 응답")
public class CartProductResponse {
    @Schema(description = "장바구니 상품 ID / 비회원: 임시 UUID")
    Long id;

    @Schema(description = "상품 ID")
    Long productId;

    @Schema(description = "상품명")
    String productName;

    @Schema(description = "상품 이미지 URL")
    String imageUrl;

    @Schema(description = "선택한 색상")
    String color;

    @Schema(description = "선택한 사이즈")
    String size;

    @Schema(description = "개당 가격")
    BigDecimal price;

    @Schema(description = "수량")
    Integer quantity;

    @Schema(description = "총 가격 (price × quantity)")
    BigDecimal totalPrice;
}