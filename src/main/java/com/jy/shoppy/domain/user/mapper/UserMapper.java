package com.jy.shoppy.domain.user.mapper;

import com.jy.shoppy.domain.order.entity.Order;
import com.jy.shoppy.domain.order.entity.OrderProduct;
import com.jy.shoppy.domain.user.entity.User;
import com.jy.shoppy.domain.user.dto.CreateUserRequest;
import com.jy.shoppy.domain.prodcut.dto.OrderProductResponse;
import com.jy.shoppy.domain.user.dto.UserOrderResponse;
import com.jy.shoppy.domain.user.dto.UserResponse;
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
