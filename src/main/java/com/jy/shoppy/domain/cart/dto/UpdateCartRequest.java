package com.jy.shoppy.domain.cart.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UpdateCartRequest {
    @NotBlank
    private Long productId;

    private int quantity;
}
