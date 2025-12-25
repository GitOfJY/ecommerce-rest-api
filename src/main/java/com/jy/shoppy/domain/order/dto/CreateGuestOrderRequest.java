package com.jy.shoppy.domain.order.dto;

import com.jy.shoppy.domain.prodcut.dto.OrderProductRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "비회원 주문 등록 요청")
public class CreateGuestOrderRequest {
    @NotNull(message = "주문 상품 목록은 필수입니다.")
    @Schema(description = "주문 상품 목록", required = true)
    private List<OrderProductRequest> products;

    @NotBlank(message = "비회원 주문 비밀번호는 필수입니다.")
    @Size(min = 4, max = 20, message = "비밀번호는 4~20자여야 합니다.")
    @Schema(description = "주문 조회용 비밀번호", example = "guest1234", required = true)
    private String guestPassword;

    @NotBlank(message = "주문자 이름은 필수입니다.")
    @Schema(description = "주문자 이름", example = "홍길동", required = true)
    private String name;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    @Schema(description = "주문자 이메일", example = "hong@example.com", required = true)
    private String email;

    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
    @Schema(description = "주문자 전화번호", example = "010-1234-5678", required = true)
    private String phone;

    @NotBlank(message = "우편번호는 필수입니다.")
    @Schema(description = "우편번호", example = "06234", required = true)
    private String zipCode;

    @NotBlank(message = "시/도는 필수입니다.")
    @Schema(description = "시/도", example = "서울시", required = true)
    private String city;

    @NotBlank(message = "배송 주소는 필수입니다.")
    @Schema(description = "도로명/지번 주소", example = "강남구 테헤란로 123", required = true)
    private String street;

    @Schema(description = "상세 주소", example = "101동 202호")
    private String detail;
}
