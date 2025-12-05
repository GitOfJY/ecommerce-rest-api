package com.jy.shoppy.service.dto;

import com.jy.shoppy.entity.type.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class UserOrderResponse {
    private LocalDateTime orderDate;
    private OrderStatus status;
    private List<OrderProductResponse> orderProducts;
}
