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
    private Integer quantity;
    private BigDecimal orderPrice;
}
