package com.jy.shoppy.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class OrderProductRequest {
    @NotNull
    private Long productId;

    @NotNull
    @Min(value = 1, message = "최소 수량은 1개입니다.")
    private Integer quantity;
}
