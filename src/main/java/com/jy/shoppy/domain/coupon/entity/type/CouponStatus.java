package com.jy.shoppy.domain.coupon.entity.type;

public enum CouponStatus {
    AVAILABLE("발급됨 - 등록 대기"),      // 생성됨, 아직 사용자 할당 안됨
    ISSUED("등록됨 - 사용 가능"),         // 사용자에게 할당됨
    USED("사용 완료"),                  // 주문에서 사용됨
    EXPIRED("만료됨");                  // 유효기간 지남

    private final String description;

    CouponStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
