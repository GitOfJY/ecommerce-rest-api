package com.jy.shoppy.domain.cart.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteCartProductRequest {
    @NotEmpty(message = "삭제할 상품을 선택해주세요.")
    private List<Long> cartProductIds;
}
