package com.jy.shoppy.domain.coupon.entity.type;

public enum DiscountType {
    PERCENT("퍼센트 할인"),
    FIXED("정액 할인");

    private final String description;

    DiscountType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 할인 금액 계산
     * @param orderAmount 주문 금액
     * @param discountValue 할인 값
     * @param maxDiscountAmount 최대 할인 금액 (null 가능)
     * @return 실제 할인 금액
     */
    public int calculateDiscount(int orderAmount, int discountValue, Integer maxDiscountAmount) {
        int discount = switch (this) {
            case PERCENT -> (int) (orderAmount * discountValue / 100.0);
            case FIXED -> discountValue;
        };

        // 최대 할인 금액 제한
        if (maxDiscountAmount != null && discount > maxDiscountAmount) {
            return maxDiscountAmount;
        }

        return discount;
    }
}
