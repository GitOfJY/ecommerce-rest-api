package com.sparta.demo.mapper;

import com.sparta.demo.entity.Order;
import com.sparta.demo.entity.OrderProduct;
import com.sparta.demo.service.dto.OrderProductResponse;
import com.sparta.demo.service.dto.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "userName",        source = "user.username")
    @Mapping(target = "shippingAddress", source = "user.address")
    @Mapping(target = "products",        source = "orderProducts")
    @Mapping(target = "totalPrice",      expression = "java(order.getTotalPrice())")
    @Mapping(target = "orderStatus",     source = "status")
    OrderResponse toResponse(Order order);

    List<OrderResponse> toResponseList(List<Order> orders);

    @Mapping(target = "productId",   source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "quantity",    source = "quantity")
    @Mapping(target = "orderPrice",  source = "orderPrice")
    OrderProductResponse toOrderItem(OrderProduct orderProduct);

    List<OrderProductResponse> toOrderItems(List<OrderProduct> orderProducts);
}