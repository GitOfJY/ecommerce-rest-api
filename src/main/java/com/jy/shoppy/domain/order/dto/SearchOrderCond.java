package com.jy.shoppy.domain.order.dto;

import com.jy.shoppy.domain.order.entity.type.OrderStatus;

import java.time.LocalDateTime;

public record SearchOrderCond (
        Long userId,
        OrderStatus orderStatus,
        LocalDateTime startDate,
        LocalDateTime endDate
) {}
