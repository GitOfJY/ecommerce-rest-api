package com.jy.shoppy.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jy.shoppy.entity.type.RefundStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RefundResponse {
    private Long refundId;
    private RefundStatus status;
    private String reason;
    private BigDecimal refundAmount;
    private LocalDateTime createdAt;

    private Long orderId;

    private Long userId;
    private String userName;
}
