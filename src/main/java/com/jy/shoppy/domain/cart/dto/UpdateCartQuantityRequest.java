package com.jy.shoppy.domain.cart.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdateCartQuantityRequest {
    @NotNull(message = "수량은 필수입니다")
    @Min(value = 1, message = "수량은 최소 1개 이상이어야 합니다")
    @Max(value = 999, message = "수량은 999개를 초과할 수 없습니다")
    private int quantity;
}
