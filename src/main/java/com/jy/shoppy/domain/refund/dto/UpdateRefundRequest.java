package com.jy.shoppy.domain.refund.dto;

import com.jy.shoppy.domain.refund.entity.type.RefundStatus;
import lombok.Getter;

@Getter
public class UpdateRefundRequest {
    private Long adminId;              // 관리자
    private RefundStatus refundStatus; // 환불 변경 상태
}
