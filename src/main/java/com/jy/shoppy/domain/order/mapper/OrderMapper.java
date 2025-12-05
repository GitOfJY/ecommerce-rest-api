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