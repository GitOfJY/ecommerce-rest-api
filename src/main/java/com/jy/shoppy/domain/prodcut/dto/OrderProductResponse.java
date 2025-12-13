package com.jy.shoppy.domain.prodcut.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderProductResponse {
    private Long productId;
    private String productName;
    // TODO : 상품에서 수량(quantity), 재고(stock) 일괄 구분하기
    private Integer quantity;
    private BigDecimal orderPrice;
}
