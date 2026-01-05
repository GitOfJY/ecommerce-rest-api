package com.jy.shoppy.domain.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jy.shoppy.domain.order.entity.type.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "주문 응답")
public class OrderResponse {
    @Schema(description = "주문 ID")
    Long id;

    @Schema(description = "주문 번호")
    String orderNumber;

    @Schema(description = "주문자명")
    String userName;

    // ========== 배송지 정보 ==========
    @Schema(description = "수령인명")
    String recipientName;

    @Schema(description = "수령인 전화번호")
    String recipientPhone;

    @Schema(description = "수령인 이메일")
    String recipientEmail;

    @Schema(description = "우편번호")
    String zipCode;

    @Schema(description = "도시")
    String city;

    @Schema(description = "도로명 주소")
    String street;

    @Schema(description = "상세 주소")
    String detail;

    @Schema(description = "전체 주소")
    String fullAddress;

    // ========== 주문 상품 목록 ==========
    @Schema(description = "주문 상품 목록")
    List<OrderProductResponse> products;

    // ========== 금액 정보 ==========
    @Schema(description = "원가 (등급 할인 전)", example = "178632")
    BigDecimal originalPrice;

    @Schema(description = "회원 등급 할인 금액", example = "8932")
    BigDecimal gradeDiscountAmount;

    @Schema(description = "회원 등급 할인율", example = "0.05")
    BigDecimal gradeDiscountRate;

    @Schema(description = "등급 할인 후 금액", example = "169700")
    BigDecimal totalPrice;

    @Schema(description = "쿠폰 할인 금액", example = "20000")
    BigDecimal couponDiscountAmount;

    @Schema(description = "적립금 사용 금액", example = "5000")
    Integer pointsUsed;

    @Schema(description = "최종 결제 금액", example = "144700")
    BigDecimal finalPrice;

    // ========== 적립금 정보 추가 ==========
    @Schema(description = "적립 예정 금액", example = "1447")
    Integer pointsToEarn;

    @Schema(description = "적립률", example = "0.01")
    BigDecimal pointRate;

    @Schema(description = "적립금 적립 후 잔액", example = "6447")
    Integer pointsBalance;

    // ========== 주문 정보 ==========
    @Schema(description = "주문 일시")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime orderDate;

    @Schema(description = "주문 상태")
    OrderStatus orderStatus;

    // ========== 사용자 정보 ==========
    @Schema(description = "회원 등급명", example = "GOLD")
    String userGradeName;

    // ========== 쿠폰 정보 개선 ==========
    @Schema(description = "적용된 쿠폰 정보")
    CouponInfo appliedCoupon;

    // ========== 내부 클래스: 주문 상품 ==========
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Schema(description = "주문 상품 정보")
    public static class OrderProductResponse {

        @Schema(description = "상품 ID")
        Long productId;

        @Schema(description = "상품명")
        String productName;

        @Schema(description = "색상")
        String color;

        @Schema(description = "사이즈")
        String size;

        @Schema(description = "수량")
        Integer quantity;

        @Schema(description = "개당 가격 (등급 할인 적용)")
        BigDecimal orderPrice;

        @Schema(description = "총 가격 (개당 가격 × 수량)")
        BigDecimal totalPrice;
    }

    // ========== 내부 클래스: 쿠폰 정보 신규 추가 ==========
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Schema(description = "적용된 쿠폰 정보")
    public static class CouponInfo {

        @Schema(description = "쿠폰 사용자 ID")
        Long couponUserId;

        @Schema(description = "쿠폰 코드", example = "VIP2024-ABC123")
        String couponCode;

        @Schema(description = "쿠폰명", example = "VIP 회원 20,000원 할인")
        String couponName;

        @Schema(description = "할인 타입", example = "FIXED")
        String discountType;

        @Schema(description = "할인 값", example = "20000")
        Integer discountValue;

        @Schema(description = "할인 금액", example = "20000")
        BigDecimal discountAmount;
    }
}