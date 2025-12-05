package com.jy.shoppy.domain.user.dto;

import com.jy.shoppy.domain.order.entity.type.OrderStatus;
import com.jy.shoppy.domain.prodcut.dto.OrderProductResponse;
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
