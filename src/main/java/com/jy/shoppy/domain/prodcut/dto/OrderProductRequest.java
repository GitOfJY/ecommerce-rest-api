package com.jy.shoppy.domain.prodcut.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class OrderProductRequest {
    @NotNull
    private Long productId;

    @Size(max = 50, message = "색상은 50자를 초과할 수 없습니다.")
    private String color;

    @Size(max = 20, message = "사이즈는 20자를 초과할 수 없습니다.")
    private String size;

    @NotNull
    @Min(value = 1, message = "최소 수량은 1개입니다.")
    private Integer quantity;
}
