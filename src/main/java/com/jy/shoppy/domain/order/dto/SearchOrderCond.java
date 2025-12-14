package com.jy.shoppy.domain.order.dto;

import com.jy.shoppy.domain.order.entity.type.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record SearchOrderCond (
        @Schema(hidden = true)
        Long userId,

        OrderStatus orderStatus,
        LocalDateTime startDate,
        LocalDateTime endDate
) {}
