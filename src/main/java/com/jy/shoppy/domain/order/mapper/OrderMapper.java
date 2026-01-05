package com.jy.shoppy.domain.order.mapper;

import com.jy.shoppy.domain.coupon.entity.CouponUser;
import com.jy.shoppy.domain.coupon.repository.CouponUserRepository;
import com.jy.shoppy.domain.order.dto.OrderResponse;
import com.jy.shoppy.domain.order.entity.Order;
import com.jy.shoppy.domain.order.entity.OrderProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderMapper {
    private final CouponUserRepository couponUserRepository;

    public OrderResponse toResponse(Order order) {
        // 1. 원가 계산 (등급 할인 전)
        BigDecimal originalPrice = calculateOriginalPrice(order);

        // 2. 등급 할인율 및 할인 금액
        BigDecimal gradeDiscountRate = getGradeDiscountRate(order);
        BigDecimal gradeDiscountAmount = originalPrice.multiply(gradeDiscountRate);

        // 3. 등급 할인 후 금액
        BigDecimal totalPrice = order.getTotalPrice();

        // 4. 쿠폰 할인 금액
        BigDecimal couponDiscountAmount = order.getCouponDiscount() != null
                ? order.getCouponDiscount()
                : BigDecimal.ZERO;

        // 5. 적립금 사용 금액
        Integer pointsUsed = order.getPointsUsed() != null ? order.getPointsUsed() : 0;

        // 6. 최종 결제 금액
        BigDecimal finalPrice = order.getFinalPrice();

        // 7. 적립 예정 금액 계산
        Integer pointsToEarn = calculatePointsToEarn(order, finalPrice);

        // 8. 적립률
        BigDecimal pointRate = getPointRate(order);

        // 9. 적립 후 잔액
        Integer pointsBalance = calculatePointsBalance(order, pointsToEarn);

        // 10. 적용된 쿠폰 정보
        OrderResponse.CouponInfo appliedCoupon = getAppliedCouponInfo(order);

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userName(order.getUserName())
                .recipientName(order.getRecipientName())
                .recipientPhone(order.getReceiverPhone())
                .recipientEmail(order.getRecipientEmail())
                .zipCode(order.getZipcode())
                .city(order.getCity())
                .street(order.getStreet())
                .detail(order.getDetail())
                .fullAddress(order.getFullAddress())
                .products(toProductResponses(order.getOrderProducts()))
                .originalPrice(originalPrice)
                .gradeDiscountAmount(gradeDiscountAmount)
                .gradeDiscountRate(gradeDiscountRate)
                .totalPrice(totalPrice)
                .couponDiscountAmount(couponDiscountAmount)
                .pointsUsed(pointsUsed)
                .finalPrice(finalPrice)
                .pointsToEarn(pointsToEarn)
                .pointRate(pointRate)
                .pointsBalance(pointsBalance)
                .orderDate(order.getOrderDate())
                .orderStatus(order.getStatus())
                .userGradeName(getUserGradeName(order))
                .appliedCoupon(appliedCoupon)
                .build();
    }

    /**
     * 주문 상품 리스트 변환
     */
    private List<OrderResponse.OrderProductResponse> toProductResponses(List<OrderProduct> orderProducts) {
        return orderProducts.stream()
                .map(this::toProductResponse)
                .collect(Collectors.toList());
    }

    /**
     * 주문 상품 단건 변환
     */
    private OrderResponse.OrderProductResponse toProductResponse(OrderProduct orderProduct) {
        return OrderResponse.OrderProductResponse.builder()
                .productId(orderProduct.getProduct().getId())
                .productName(orderProduct.getProduct().getName())
                .color(orderProduct.getSelectedColor())
                .size(orderProduct.getSelectedSize())
                .quantity(orderProduct.getQuantity())
                .orderPrice(orderProduct.getOrderPrice())
                .totalPrice(orderProduct.getTotalPrice())
                .build();
    }

    /**
     * 원가 계산 (등급 할인 전)
     */
    private BigDecimal calculateOriginalPrice(Order order) {
        BigDecimal gradeDiscountRate = getGradeDiscountRate(order);

        if (gradeDiscountRate.compareTo(BigDecimal.ZERO) == 0) {
            return order.getTotalPrice();
        }

        // 역산: totalPrice = originalPrice × (1 - rate)
        // originalPrice = totalPrice / (1 - rate)
        BigDecimal multiplier = BigDecimal.ONE.subtract(gradeDiscountRate);
        return order.getTotalPrice().divide(multiplier, 0, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 회원 등급 할인율 조회
     */
    private BigDecimal getGradeDiscountRate(Order order) {
        if (order.getUser() == null || order.getUser().getUserGrade() == null) {
            return BigDecimal.ZERO;
        }
        return order.getUser().getUserGrade().getDiscountRate();
    }

    /**
     * 회원 등급명 조회
     */
    private String getUserGradeName(Order order) {
        if (order.getUser() == null || order.getUser().getUserGrade() == null) {
            return "GUEST";
        }
        return order.getUser().getUserGrade().getName();
    }

    /**
     * 주문 리스트 변환
     */
    public List<OrderResponse> toResponseList(List<Order> orders) {
        return orders.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 적립률 조회
     */
    private BigDecimal getPointRate(Order order) {
        if (order.getUser() == null || order.getUser().getUserGrade() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal rate = order.getUser().getUserGrade().getPointRate();
        return rate != null ? rate : BigDecimal.ZERO;
    }

    /**
     * 적립 예정 금액 계산
     */
    private Integer calculatePointsToEarn(Order order, BigDecimal finalPrice) {
        BigDecimal pointRate = getPointRate(order);

        if (pointRate.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }

        // 최종 결제 금액 × 적립률 (소수점 버림)
        BigDecimal earnAmount = finalPrice.multiply(pointRate)
                .setScale(0, RoundingMode.DOWN);

        return earnAmount.intValue();
    }

    /**
     * 적립 후 잔액 계산
     */
    private Integer calculatePointsBalance(Order order, Integer pointsToEarn) {
        if (order.getUser() == null) {
            return null;
        }

        // 현재 잔액 + 적립 예정 금액
        return order.getUser().getPoints() + pointsToEarn;
    }

    /**
     * 적용된 쿠폰 정보 조회
     */
    private OrderResponse.CouponInfo getAppliedCouponInfo(Order order) {
        if (order.getCouponUserId() == null) {
            return null;
        }

        // 쿠폰 정보 조회
        CouponUser couponUser = couponUserRepository.findByIdWithCoupon(order.getCouponUserId())
                .orElse(null);

        if (couponUser == null) {
            return null;
        }

        return OrderResponse.CouponInfo.builder()
                .couponUserId(couponUser.getId())
                .couponCode(couponUser.getCode())
                .couponName(couponUser.getCoupon().getName())
                .discountType(couponUser.getCoupon().getDiscountType().name())
                .discountValue(couponUser.getCoupon().getDiscountValue())
                .discountAmount(order.getCouponDiscount())
                .build();
    }
}