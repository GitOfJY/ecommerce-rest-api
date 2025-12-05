package com.jy.shoppy.domain.refund.dto;

import com.jy.shoppy.domain.refund.entity.type.RefundStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateRefundResponse {
        // 환불 정보
        private Long refundId;
        private RefundStatus status;
        private String reason;
        private BigDecimal refundAmount;
        private LocalDateTime createdAt;

        // 주문 정보
        private Long orderId;

        // 사용자 정보
        private Long userId;
        private String userName;
}
