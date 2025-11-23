package com.sparta.demo.service.dto;

import com.sparta.demo.entity.type.OrderStatus;

import java.time.LocalDateTime;

public record SearchOrderCond (
        Long userId,
        OrderStatus orderStatus,
        LocalDateTime startDate,
        LocalDateTime endDate
) {}
