package com.jy.shoppy.domain.coupon.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IssueCouponRequest {
    @NotNull(message = "발급 개수는 필수입니다")
    @Min(value = 1, message = "발급 개수는 1개 이상이어야 합니다")
    @Max(value = 10000, message = "한 번에 최대 10,000개까지 발급 가능합니다")
    Integer quantity;
}
