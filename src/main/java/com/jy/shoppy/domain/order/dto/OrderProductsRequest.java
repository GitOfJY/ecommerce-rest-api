package com.jy.shoppy.domain.order.dto;

import com.jy.shoppy.domain.prodcut.dto.OrderProductRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "주문 상품 정보 (공통)")
public class OrderProductsRequest {
    @NotNull(message = "주문 상품 목록은 필수입니다.")
    @Schema(description = "주문 상품 목록", required = true)
    private List<OrderProductRequest> products;
}
