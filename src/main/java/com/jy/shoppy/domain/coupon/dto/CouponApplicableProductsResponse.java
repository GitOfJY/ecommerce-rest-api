package com.jy.shoppy.domain.coupon.dto;

import com.jy.shoppy.domain.coupon.entity.type.CouponApplicationType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CouponApplicableProductsResponse {
    String couponCode;
    String couponName;
    CouponApplicationType applicationType;
    String applicationTypeDescription;

    // applicationType이 PRODUCT일 때만 값이 있음
    List<CouponProductResponse> products;

    // applicationType이 CATEGORY일 때만 값이 있음
    List<CouponCategoryResponse> categories;
}
