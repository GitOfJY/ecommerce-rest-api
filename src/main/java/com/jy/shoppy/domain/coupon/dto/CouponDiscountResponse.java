package com.jy.shoppy.domain.coupon.dto;

import com.jy.shoppy.domain.coupon.entity.CouponUser;
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
public class CouponDiscountResponse {
    Long couponUserId;
    String couponCode;
    String couponName;
    BigDecimal originalAmount;      // 원래 금액
    BigDecimal discountAmount;      // 할인 금액
    BigDecimal finalAmount;         // 최종 금액
    Boolean isApplicable;        // 적용 가능 여부
    String notApplicableReason;  // 적용 불가 사유

    /**
     * 적용 불가능한 쿠폰 응답 생성 (정적 팩토리 메서드)
     */
    public static CouponDiscountResponse notApplicable(
            CouponUser couponUser,
            BigDecimal totalAmount,
            String reason
    ) {
        return CouponDiscountResponse.builder()
                .couponUserId(couponUser.getId())
                .couponCode(couponUser.getCode())
                .couponName(couponUser.getCoupon().getName())
                .originalAmount(totalAmount)
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(totalAmount)
                .isApplicable(false)
                .notApplicableReason(reason)
                .build();
    }

    /**
     * 적용 가능한 쿠폰 응답 생성 (정적 팩토리 메서드)
     */
    public static CouponDiscountResponse applicable(
            CouponUser couponUser,
            BigDecimal originalAmount,
            BigDecimal discountAmount
    ) {
        return CouponDiscountResponse.builder()
                .couponUserId(couponUser.getId())
                .couponCode(couponUser.getCode())
                .couponName(couponUser.getCoupon().getName())
                .originalAmount(originalAmount)
                .discountAmount(discountAmount)
                .finalAmount(originalAmount.subtract(discountAmount))
                .isApplicable(true)
                .notApplicableReason(null)
                .build();
    }
}
