package com.jy.shoppy.domain.cart.dto;

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
    // 이름, 가격, 수량, TODO : (배송정보, 배송비, 상품옵션) 추가
    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalPrice;

    public void addQuantity(int quantity) {
        this.quantity += quantity;
    }
}