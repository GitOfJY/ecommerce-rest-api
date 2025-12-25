package com.jy.shoppy.domain.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "비회원 주문 조회 요청")
public class GuestOrderRequest {
    @NotBlank(message = "주문번호는 필수입니다.")
    @Schema(description = "주문번호", example = "ORD202412267F3B2A1C", required = true)
    private String orderNumber;

    @NotBlank(message = "주문자명은 필수입니다.")
    @Schema(description = "주문자명", example = "홍길동", required = true)
    private String name;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Schema(description = "주문 조회 비밀번호", example = "guest1234", required = true)
    private String password;
}
