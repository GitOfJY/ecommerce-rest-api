package com.jy.shoppy.domain.order.dto;

import com.jy.shoppy.domain.prodcut.dto.OrderProductResponse;
import com.jy.shoppy.domain.order.entity.type.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "주문 응답")
public class OrderResponse {
    @Schema(description = "주문 ID")
    private Long id;

    @Schema(description = "주문 번호", example = "ORD202412267F3B2A1C")
    private String orderNumber;

    @Schema(description = "주문자명", example = "홍길동")
    private String userName;

    @Schema(description = "수령인 이름", example = "홍길동")
    private String recipientName;

    @Schema(description = "수령인 전화번호", example = "010-1234-5678")
    private String recipientPhone;

    @Schema(description = "수령인 이메일", example = "hong@example.com")
    private String recipientEmail;

    @Schema(description = "우편번호", example = "06234")
    private String zipCode;

    @Schema(description = "시/도", example = "서울시")
    private String city;

    @Schema(description = "도로명/지번 주소", example = "강남구 테헤란로 123")
    private String street;

    @Schema(description = "상세 주소", example = "101동 202호")
    private String detail;

    @Schema(description = "전체 주소", example = "[06234] 서울시 강남구 테헤란로 123 101동 202호")
    private String fullAddress;

    @Schema(description = "주문 상품 목록")
    private List<OrderProductResponse> products;

    @Schema(description = "총 주문 금액")
    private BigDecimal totalPrice;

    @Schema(description = "주문 일시")
    private LocalDateTime orderDate;

    @Schema(description = "주문 상태")
    private OrderStatus orderStatus;
}