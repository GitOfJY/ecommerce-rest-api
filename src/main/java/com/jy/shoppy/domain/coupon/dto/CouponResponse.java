package com.jy.shoppy.domain.coupon.dto;

import com.jy.shoppy.domain.coupon.entity.type.DiscountType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CouponResponse {
    Long id;
    String name;
    DiscountType discountType;
    String discountTypeDescription;
    Integer discountValue;
    Integer minOrderAmount;
    Integer maxDiscountAmount;
    Integer validDays;
    LocalDateTime startDate;
    LocalDateTime endDate;
    Integer usageLimit;
    Integer issueCount;
    Integer usedCount;
    Integer remainingCount;
    Boolean isValid;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
