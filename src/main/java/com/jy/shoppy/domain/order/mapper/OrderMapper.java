package com.jy.shoppy.domain.order.mapper;

import com.jy.shoppy.domain.order.entity.Order;
import com.jy.shoppy.domain.order.entity.OrderProduct;
import com.jy.shoppy.domain.prodcut.dto.OrderProductResponse;
import com.jy.shoppy.domain.order.dto.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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

    @Mapping(target = "productId",   source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "quantity",    source = "quantity")
    @Mapping(target = "orderPrice",  source = "orderPrice")
    OrderProductResponse toOrderItem(OrderProduct orderProduct);

    List<OrderProductResponse> toOrderItems(List<OrderProduct> orderProducts);
}