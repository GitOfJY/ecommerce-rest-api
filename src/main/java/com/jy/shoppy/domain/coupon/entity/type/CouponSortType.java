package com.jy.shoppy.domain.coupon.entity.type;

public enum CouponSortType {
    LATEST("최신순"),            // created_at DESC
    DISCOUNT_DESC("할인 많은순"), // 할인 금액 높은 순
    EXPIRY_ASC("만료 임박순");    // end_date ASC

    private final String description;

    CouponSortType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
