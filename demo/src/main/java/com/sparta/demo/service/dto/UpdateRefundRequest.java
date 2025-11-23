package com.sparta.demo.service.dto;

import com.sparta.demo.entity.type.RefundStatus;
import lombok.Getter;

@Getter
public class UpdateRefundRequest {
    private Long adminId;              // 관리자
    private RefundStatus refundStatus; // 환불 변경 상태
}
