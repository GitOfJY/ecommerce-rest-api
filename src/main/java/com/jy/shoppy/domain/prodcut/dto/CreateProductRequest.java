package com.jy.shoppy.domain.prodcut.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
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
public class CreateProductRequest {
    @NotBlank(message = "상품명은 필수입니다.")
    @Size(max = 50, message = "상품명은 50자 이하로 입력해주세요.")
    private String name;

    @NotBlank
    private String description;

    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    private BigDecimal price;

    @Valid
    @NotEmpty(message = "옵션은 최소 1개 이상 등록해야 합니다.")
    private List<ProductOptionRequest> options;

    @NotEmpty
    private List<Long> categoryIds;

    public boolean hasOptions() {
        if (options.size() == 1) {
            ProductOptionRequest first = options.get(0);
            return first.getColor() != null || first.getSize() != null;
        }
        return true;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductOptionRequest {
        @Size(max = 50, message = "색상은 50자를 초과할 수 없습니다.")
        private String color;

        @Size(max = 20, message = "사이즈는 20자를 초과할 수 없습니다.")
        private String size;

        @NotNull(message = "재고는 필수입니다.")
        @Min(value = 0, message = "재고는 0 이상이어야 합니다.")
        private Integer stock;

        @NotNull(message = "추가 금액은 필수입니다.")
        @DecimalMin(value = "0.0", message = "추가 금액은 0 이상이어야 합니다.")
        private BigDecimal additionalPrice;
    }
}
