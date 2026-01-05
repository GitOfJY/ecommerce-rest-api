package com.jy.shoppy.domain.point.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PointType {
    EARN("적립", "주문 완료 시 적립"),
    USE("사용", "주문 시 사용"),
    CANCEL_EARN("적립 취소", "주문 취소로 인한 적립금 회수"),
    CANCEL_USE("사용 취소", "주문 취소로 인한 적립금 반환"),
    EXPIRE("만료", "적립금 유효기간 만료"),
    ADMIN("관리자 지급", "관리자가 직접 지급");

    private final String description;
    private final String detail;
}
