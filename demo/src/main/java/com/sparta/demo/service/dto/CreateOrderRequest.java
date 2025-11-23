package com.sparta.demo.service.dto;

import com.sparta.demo.entity.OrderProduct;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {
    @NotNull
    private Long userId;

    @NotNull
    private List<OrderProductRequest> products;

    @NotBlank
    private String shippingAddress;
}