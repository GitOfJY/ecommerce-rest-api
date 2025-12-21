package com.jy.shoppy.domain.prodcut.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductOptionRequest {
    private String color;

    private String size;

    @NotNull(message = "재고를 입력해주세요")
    @Min(value = 0, message = "재고는 0 이상이어야 합니다")
    private Integer stock;

    @Min(value = 0, message = "추가 금액은 0 이상이어야 합니다")
    private BigDecimal additionalPrice;
}
