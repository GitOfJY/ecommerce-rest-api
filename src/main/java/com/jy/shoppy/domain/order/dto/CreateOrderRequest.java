package com.jy.shoppy.domain.order.dto;

import com.jy.shoppy.domain.prodcut.dto.OrderProductRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Email;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "주문 등록 요청 (회원/비회원 공통)")
public class CreateOrderRequest {
    @NotNull(message = "주문 상품 목록은 필수입니다.")
    @Schema(description = "주문 상품 목록", example = "[{\"productId\": 1008, \"quantity\": 2}]", required = true)
    private List<OrderProductRequest> products;

    @Schema(
            description = "비회원 주문 비밀번호 (비회원 주문시 필수, 회원 주문시 null)",
            example = "guest1234",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String guestPassword;

    @NotBlank(message = "수령인 이름은 필수입니다.")
    @Schema(description = "수령인 이름", example = "홍길동", required = true)
    private String recipientName;

    @NotBlank(message = "수령인 전화번호는 필수입니다.")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
    @Schema(description = "수령인 전화번호", example = "010-1234-5678", required = true)
    private String recipientPhone;

    @NotBlank(message = "수령인 이메일은 필수입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    @Schema(description = "수령인 이메일", example = "hong@example.com", required = true)
    private String recipientEmail;

    @Schema(description = "우편번호", example = "06234")
    private String zipCode;

    @Schema(description = "시/도", example = "서울시")
    private String city;

    @NotBlank(message = "배송 주소는 필수입니다.")
    @Schema(description = "도로명/지번 주소", example = "강남구 테헤란로 123", required = true)
    private String street;

    @Schema(description = "상세 주소", example = "101동 202호")
    private String detail;
}