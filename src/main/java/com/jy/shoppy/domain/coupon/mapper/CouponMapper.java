package com.jy.shoppy.domain.coupon.mapper;

import com.jy.shoppy.domain.coupon.dto.CouponResponse;
import com.jy.shoppy.domain.coupon.dto.IssueCouponResponse;
import com.jy.shoppy.domain.coupon.entity.Coupon;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CouponMapper {
    /**
     * Coupon Entity → CouponResponse DTO
     */
    @Mapping(target = "discountTypeDescription", expression = "java(coupon.getDiscountType().getDescription())")
    @Mapping(target = "remainingCount", expression = "java(coupon.getRemainingCount())")
    @Mapping(target = "isValid", expression = "java(coupon.isValid())")
    CouponResponse toCouponResponse(Coupon coupon);

    /**
     * Coupon + 발급 정보 → IssueCouponResponse
     */
    @Mapping(target = "couponId", source = "coupon.id")
    @Mapping(target = "couponName", source = "coupon.name")
    @Mapping(target = "totalIssueCount", source = "coupon.issueCount")
    @Mapping(target = "remainingCount", expression = "java(coupon.getRemainingCount())")
    @Mapping(target = "couponCodes", expression = "java(limitCouponCodes(couponCodes))")
    IssueCouponResponse toIssueCouponResponse(
            Coupon coupon,
            Integer issuedCount,
            List<String> couponCodes,
            LocalDateTime expiresAt
    );
}
