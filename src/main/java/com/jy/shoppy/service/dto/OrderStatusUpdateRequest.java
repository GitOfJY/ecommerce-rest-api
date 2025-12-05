package com.jy.shoppy.service.dto;

import com.jy.shoppy.entity.type.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusUpdateRequest {
    @NotNull
    private Long orderId;

    @NotNull
    private OrderStatus status; // COMPLETED or CANCELED
}