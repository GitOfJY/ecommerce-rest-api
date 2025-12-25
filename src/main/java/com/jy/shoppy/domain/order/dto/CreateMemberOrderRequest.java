package com.jy.shoppy.domain.order.dto;

import com.jy.shoppy.domain.prodcut.dto.OrderProductRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "회원 주문 등록 요청")
public class CreateMemberOrderRequest {
    @NotNull(message = "주문 상품 목록은 필수입니다.")
    @Schema(description = "주문 상품 목록", required = true)
    private List<OrderProductRequest> products;

    // 배송지 선택 (있으면 기존 배송지, 없으면 새로 생성)
    @Schema(description = "배송지 ID (기존 배송지 사용 시)", example = "1")
    private Long deliveryAddressId;

    @Schema(description = "수령인 이름 (새 배송지)", example = "홍길동")
    private String recipientName;

    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
    @Schema(description = "수령인 전화번호 (새 배송지)", example = "010-1234-5678")
    private String recipientPhone;

    @Email(message = "유효한 이메일 형식이 아닙니다.")
    @Schema(description = "수령인 이메일 (새 배송지)", example = "hong@example.com")
    private String recipientEmail;

    @Schema(description = "우편번호 (새 배송지)", example = "06234")
    private String zipCode;

    @Schema(description = "시/도 (새 배송지)", example = "서울시")
    private String city;

    @Schema(description = "도로명/지번 주소 (새 배송지)", example = "강남구 테헤란로 123")
    private String street;

    @Schema(description = "상세 주소 (새 배송지)", example = "101동 202호")
    private String detail;
}
