package com.jy.shoppy.domain.coupon.mapper;

import com.jy.shoppy.domain.coupon.dto.CouponResponse;
import com.jy.shoppy.domain.coupon.dto.IssueCouponResponse;
import com.jy.shoppy.domain.coupon.dto.RegisterCouponResponse;
import com.jy.shoppy.domain.coupon.dto.UserCouponResponse;
import com.jy.shoppy.domain.coupon.entity.Coupon;
import com.jy.shoppy.domain.coupon.entity.CouponUser;
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

    /**
     * CouponUser → RegisterCouponResponse
     */
    @Mapping(target = "couponUserId", source = "id")
    @Mapping(target = "couponCode", source = "code")
    @Mapping(target = "couponName", source = "coupon.name")
    @Mapping(target = "discountTypeDescription", source = "coupon.discountType.description")
    @Mapping(target = "discountValue", source = "coupon.discountValue")
    @Mapping(target = "minOrderAmount", source = "coupon.minOrderAmount")
    @Mapping(target = "maxDiscountAmount", source = "coupon.maxDiscountAmount")
    @Mapping(target = "registeredAt", expression = "java(java.time.LocalDateTime.now())")
    RegisterCouponResponse toRegisterCouponResponse(CouponUser couponUser);

    /**
     * CouponUser → UserCouponResponse
     */
    @Mapping(target = "couponUserId", source = "id")
    @Mapping(target = "couponId", source = "coupon.id")
    @Mapping(target = "couponCode", source = "code")
    @Mapping(target = "couponName", source = "coupon.name")
    @Mapping(target = "discountType", source = "coupon.discountType")
    @Mapping(target = "discountTypeDescription", source = "coupon.discountType.description")
    @Mapping(target = "discountValue", source = "coupon.discountValue")
    @Mapping(target = "minOrderAmount", source = "coupon.minOrderAmount")
    @Mapping(target = "maxDiscountAmount", source = "coupon.maxDiscountAmount")
    @Mapping(target = "isExpired", expression = "java(couponUser.isExpired())")
    @Mapping(target = "isAvailable", expression = "java(couponUser.isAvailable())")
    UserCouponResponse toUserCouponResponse(CouponUser couponUser);

    /**
     * CouponUser List → UserCouponResponse List
     */
    List<UserCouponResponse> toUserCouponResponseList(List<CouponUser> couponUsers);

    /**
     * 쿠폰 코드 목록 제한 (첫 10개만)
     */
    default List<String> limitCouponCodes(List<String> couponCodes) {
        if (couponCodes == null || couponCodes.isEmpty()) {
            return couponCodes;
        }
        return couponCodes.size() > 10 ? couponCodes.subList(0, 10) : couponCodes;
    }
}
