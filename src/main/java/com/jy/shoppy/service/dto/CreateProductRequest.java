package com.jy.shoppy.service.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {
    @NotBlank(message = "상품명은 필수입니다.")
    @Size(max = 50, message = "상품명은 50자 이하로 입력해주세요.")
    private String name;

    @NotBlank
    private String description;

    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    private BigDecimal price;

    @NotNull
    @Min(0)
    private Integer stock;

    @NotEmpty
    private List<Long> categoryIds;
}
