package com.sparta.demo.service.dto;

import com.sparta.demo.entity.OrderProduct;
import com.sparta.demo.entity.type.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;

    private String userName;
    private String shippingAddress;

    private List<OrderProductResponse> products;
    private BigDecimal totalPrice;

    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
}