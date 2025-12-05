package com.jy.shoppy.entity.type;

public enum RefundStatus {
    PENDING,  // 요청 접수(대기)
    APPROVED, // 승인
    REJECTED, // 거절
    COMPLETED // 완료
}
