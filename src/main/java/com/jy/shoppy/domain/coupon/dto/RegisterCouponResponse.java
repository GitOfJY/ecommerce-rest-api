package com.jy.shoppy.domain.coupon.dto;

import com.jy.shoppy.domain.coupon.entity.type.CouponStatus;
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
public class RegisterCouponResponse {
    Long couponUserId;
    String couponCode;
    String couponName;
    String discountTypeDescription;
    Integer discountValue;
    Integer minOrderAmount;
    Integer maxDiscountAmount;
    LocalDateTime expiresAt;
    CouponStatus status;
    LocalDateTime registeredAt;
}
