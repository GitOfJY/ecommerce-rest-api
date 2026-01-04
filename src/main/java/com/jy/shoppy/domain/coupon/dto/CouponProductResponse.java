package com.jy.shoppy.domain.coupon.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CouponProductResponse {
    Long productId;
    String productName;
    BigDecimal price;
    String imageUrl;
}
