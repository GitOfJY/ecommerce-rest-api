package com.jy.shoppy.service.dto;

import com.jy.shoppy.entity.type.OrderStatus;

import java.time.LocalDateTime;

public record SearchOrderCond (
        Long userId,
        OrderStatus orderStatus,
        LocalDateTime startDate,
        LocalDateTime endDate
) {}
