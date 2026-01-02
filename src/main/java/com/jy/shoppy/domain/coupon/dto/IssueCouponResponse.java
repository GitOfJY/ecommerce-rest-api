package com.jy.shoppy.domain.coupon.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IssueCouponResponse {
    Long couponId;
    String couponName;
    Integer issuedCount;
    Integer totalIssueCount;
    Integer remainingCount;
    List<String> couponCodes;  // 생성된 쿠폰 코드 목록
    LocalDateTime expiresAt;
}
