package com.jy.shoppy.domain.order.dto;

import com.jy.shoppy.domain.prodcut.dto.OrderProductRequest;
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