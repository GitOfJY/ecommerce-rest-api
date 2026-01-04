package com.jy.shoppy.domain.coupon.dto;

import com.jy.shoppy.domain.prodcut.entity.Product;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

/**
 * 주문 금액 계산 결과
 */
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderCalculation {
    List<Product> products;
    BigDecimal totalAmount;
}
