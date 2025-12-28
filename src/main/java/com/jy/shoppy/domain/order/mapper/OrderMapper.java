package com.jy.shoppy.domain.order.mapper;

import com.jy.shoppy.domain.order.entity.Order;
import com.jy.shoppy.domain.order.entity.OrderProduct;
import com.jy.shoppy.domain.prodcut.dto.OrderProductResponse;
import com.jy.shoppy.domain.order.dto.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "userName", source = "user.username")
    @Mapping(target = "products", source = "orderProducts")
    @Mapping(target = "totalPrice", expression = "java(order.getTotalPrice())")
    @Mapping(target = "orderStatus", source = "status")
    @Mapping(target = "recipientName", expression = "java(order.getRecipientName())")
    @Mapping(target = "recipientPhone", expression = "java(order.getReceiverPhone())")
    @Mapping(target = "recipientEmail", expression = "java(getRecipientEmail(order))")
    @Mapping(target = "zipCode", expression = "java(order.getZipcode())")
    @Mapping(target = "city", expression = "java(order.getCity())")
    @Mapping(target = "street", expression = "java(order.getStreet())")
    @Mapping(target = "detail", expression = "java(order.getDetail())")
    @Mapping(target = "fullAddress", expression = "java(getFullAddress(order))")
    @Mapping(target = "originalPrice", expression = "java(calculateOriginalPrice(order))")
    @Mapping(target = "discountAmount", expression = "java(calculateDiscountAmount(order))")
    @Mapping(target = "discountRate", expression = "java(getDiscountRate(order))")
    @Mapping(target = "userGrade", expression = "java(getUserGrade(order))")
    OrderResponse toResponse(Order order);

    List<OrderResponse> toResponseList(List<Order> orders);

    default String getRecipientEmail(Order order) {
        if (order.isGuestOrder()) {
            return order.getGuest().getEmail();
        }
        return order.getDeliveryAddress() != null ?
                order.getDeliveryAddress().getRecipientEmail() : null;
    }

    default String getFullAddress(Order order) {
        StringBuilder address = new StringBuilder();
        if (order.getZipcode() != null) {
            address.append("[").append(order.getZipcode()).append("] ");
        }
        if (order.getCity() != null) {
            address.append(order.getCity()).append(" ");
        }
        if (order.getStreet() != null) {
            address.append(order.getStreet());
        }
        if (order.getDetail() != null && !order.getDetail().isBlank()) {
            address.append(" ").append(order.getDetail());
        }
        return address.toString();
    }

    default BigDecimal calculateOriginalPrice(Order order) {
        if (order.getUser() == null || order.getUser().getUserGrade() == null) {
            return order.getTotalPrice(); // 비회원이거나 등급 없으면 현재 가격이 원가
        }

        BigDecimal discountRate = order.getUser().getUserGrade().getDiscountRate();
        if (discountRate == null || discountRate.compareTo(BigDecimal.ZERO) == 0) {
            return order.getTotalPrice();
        }

        // 할인된 가격 = 원가 × (1 - 할인율)
        // 원가 = 할인된 가격 ÷ (1 - 할인율)
        BigDecimal multiplier = BigDecimal.ONE.subtract(discountRate);
        return order.getTotalPrice().divide(multiplier, 0, BigDecimal.ROUND_HALF_UP);
    }

    // 할인 금액 계산
    default BigDecimal calculateDiscountAmount(Order order) {
        BigDecimal originalPrice = calculateOriginalPrice(order);
        return originalPrice.subtract(order.getTotalPrice());
    }

    // 할인율 가져오기
    default BigDecimal getDiscountRate(Order order) {
        if (order.getUser() == null || order.getUser().getUserGrade() == null) {
            return BigDecimal.ZERO;
        }
        return order.getUser().getUserGrade().getDiscountRate();
    }

    // 회원 등급 가져오기
    default String getUserGrade(Order order) {
        if (order.getUser() == null || order.getUser().getUserGrade() == null) {
            return null;
        }
        return order.getUser().getUserGrade().getName();
    }

    @Mapping(target = "productId",   source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "quantity",    source = "quantity")
    @Mapping(target = "orderPrice",  source = "orderPrice")
    OrderProductResponse toOrderItem(OrderProduct orderProduct);

    List<OrderProductResponse> toOrderItems(List<OrderProduct> orderProducts);
}