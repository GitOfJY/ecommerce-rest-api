package com.jy.shoppy.domain.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCartOptionRequest {
    private String color;
    private String size;
    private Integer quantity;
}
