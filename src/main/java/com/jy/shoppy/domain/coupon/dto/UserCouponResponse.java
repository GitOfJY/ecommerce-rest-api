package com.jy.shoppy.domain.coupon.dto;

import com.jy.shoppy.domain.coupon.entity.type.CouponStatus;
import com.jy.shoppy.domain.coupon.entity.type.DiscountType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCouponResponse {
    // CouponUser 정보
    Long couponUserId;
    String couponCode;
    CouponStatus status;
    LocalDateTime issuedAt;
    LocalDateTime expiresAt;
    LocalDateTime usedAt;
    Long orderId;

    // Coupon 정보
    Long couponId;
    String couponName;
    DiscountType discountType;
    String discountTypeDescription;
    Integer discountValue;
    Integer minOrderAmount;
    Integer maxDiscountAmount;

    // 계산된 정보
    Boolean isExpired;
    Boolean isAvailable;
}
