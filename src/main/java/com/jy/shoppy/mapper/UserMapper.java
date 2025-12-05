package com.jy.shoppy.mapper;

import com.jy.shoppy.entity.Order;
import com.jy.shoppy.entity.OrderProduct;
import com.jy.shoppy.entity.User;
import com.jy.shoppy.service.dto.CreateUserRequest;
import com.jy.shoppy.service.dto.OrderProductResponse;
import com.jy.shoppy.service.dto.UserOrderResponse;
import com.jy.shoppy.service.dto.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(CreateUserRequest req);

    @Mapping(target = "userOrders", source = "orders")
    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);

    @Mapping(target="orderDate", source="orderDate")
    @Mapping(target = "status", source = "status")
    UserOrderResponse toResponseUserOrder(Order order);

    @Mapping(target="orderDate", source="orderDate")
    @Mapping(target = "status", source = "status")
    List<UserOrderResponse> toResponseUserOrders(List<Order> order);

    @Mapping(target = "productId", source = "id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "orderPrice", source = "orderPrice")
    OrderProductResponse toResponseOrderProduct(OrderProduct orderProduct);

    @Mapping(target = "productId", source = "id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "orderPrice", source = "orderPrice")
    List<OrderProductResponse> toResponseOrderProducts(List<OrderProduct> orderProducts);
}
