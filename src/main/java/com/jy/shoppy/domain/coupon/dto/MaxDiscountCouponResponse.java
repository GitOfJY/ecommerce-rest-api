package com.jy.shoppy.domain.coupon.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MaxDiscountCouponResponse {
    BigDecimal totalAmount;                    // 총 주문 금액
    BigDecimal maxDiscountAmount;              // 최대 할인 금액
    BigDecimal finalAmount;                    // 최종 결제 금액

    // 최대 할인 쿠폰
    CouponDiscountResponse bestCoupon;

    // 모든 적용 가능 쿠폰 (할인순 정렬)
    List<CouponDiscountResponse> applicableCoupons;
}
