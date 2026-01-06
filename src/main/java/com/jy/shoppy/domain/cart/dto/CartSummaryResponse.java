package com.jy.shoppy.domain.cart.dto;

import com.jy.shoppy.domain.coupon.dto.CouponDiscountResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "장바구니 결제 정보 응답")
public class CartSummaryResponse {
    @Schema(description = "상품 총액 (할인 전)")
    private BigDecimal totalAmount;

    @Schema(description = "회원 등급 할인율 (%)")
    private BigDecimal memberGradeDiscountRate;

    @Schema(description = "회원 등급 할인 금액")
    private BigDecimal memberGradeDiscountAmount;

    @Schema(description = "최대 쿠폰 할인 금액")
    private BigDecimal maxCouponDiscountAmount;

    @Schema(description = "총 할인 금액 (등급 할인 + 쿠폰 할인)")
    private BigDecimal totalDiscountAmount;

    @Schema(description = "최종 결제 금액")
    private BigDecimal finalPaymentAmount;

    @Schema(description = "적립 예정 포인트")
    private Integer expectedPoints;

    @Schema(description = "적립률 (%)")
    private BigDecimal pointRate;

    @Schema(description = "최대 할인 쿠폰 정보")
    private CouponDiscountResponse bestCoupon;

    @Schema(description = "적용 가능한 쿠폰 목록 (할인순)")
    private List<CouponDiscountResponse> applicableCoupons;

    @Schema(description = "장바구니 상품 목록")
    private List<CartProductResponse> cartProducts;
}
