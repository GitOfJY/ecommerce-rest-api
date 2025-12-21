package com.jy.shoppy.domain.cart.dto;

import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartProductResponse {
    // 회원: CartProduct.id, 비회원: 임시 UUID
    private Long id;

    private Long productId;
    private String productName;

    private String color;
    private String size;
    private Integer quantity;
    private BigDecimal additionalPrice;

    private BigDecimal price;
    private BigDecimal totalPrice;

    // 수량 증가 (장바구니 담기에서 사용)
    public void addQuantity(int quantity) {
        this.quantity += quantity;
        this.totalPrice = this.price.multiply(BigDecimal.valueOf(this.quantity));
    }

    public void updateOptions(String color, String size, int quantity) {
        this.color = color;
        this.size = size;
        if (quantity <= 0) {
            throw new ServiceException(ServiceExceptionCode.INVALID_QUANTITY);
        }
        this.quantity = quantity;
        this.totalPrice = this.price.multiply(BigDecimal.valueOf(this.quantity));
    }
}