package com.jy.shoppy.domain.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartProductRequest {
    @NotNull
    @NotNull(message = "상품 ID는 필수입니다")
    private Long productId;

    private String color;

    private String size;

    @NotNull
    @Min(value = 1, message = "최소 수량은 1개입니다.")
    private int quantity;
}
