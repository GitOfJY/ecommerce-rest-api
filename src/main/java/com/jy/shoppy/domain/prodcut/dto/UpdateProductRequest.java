package com.jy.shoppy.domain.prodcut.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductRequest {
    @NotBlank(message = "상품명을 입력해주세요")
    private String name;

    @NotBlank(message = "상품 설명을 입력해주세요")
    private String description;

    @NotNull(message = "가격을 입력해주세요")
    @Min(value = 0, message = "가격은 0 이상이어야 합니다")
    private BigDecimal price;

    private List<Long> categoryIds;

    @Valid
    private List<UpdateProductOptionRequest> options;
}
