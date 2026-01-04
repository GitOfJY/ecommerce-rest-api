package com.jy.shoppy.domain.coupon.entity.type;

public enum CouponApplicationType {
    ALL("전체 상품"),
    CATEGORY("특정 카테고리"),
    PRODUCT("특정 상품");

    private final String description;

    CouponApplicationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
