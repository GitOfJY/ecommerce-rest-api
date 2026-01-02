package com.jy.shoppy.domain.coupon.dto;

import com.jy.shoppy.domain.coupon.entity.type.DiscountType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateCouponRequest {
    @NotBlank(message = "쿠폰명은 필수입니다")
    @Size(max = 100, message = "쿠폰명은 100자를 초과할 수 없습니다")
    String name;

    @NotNull(message = "할인 타입은 필수입니다")
    DiscountType discountType;

    @NotNull(message = "할인 값은 필수입니다")
    @Min(value = 1, message = "할인 값은 1 이상이어야 합니다")
    Integer discountValue;

    @Min(value = 0, message = "최소 주문 금액은 0 이상이어야 합니다")
    Integer minOrderAmount;

    @Min(value = 0, message = "최대 할인 금액은 0 이상이어야 합니다")
    Integer maxDiscountAmount;

    @NotNull(message = "유효 기간(일)은 필수입니다")
    @Min(value = 1, message = "유효 기간은 1일 이상이어야 합니다")
    Integer validDays;

    LocalDateTime startDate;

    LocalDateTime endDate;

    @Min(value = 1, message = "발급 제한 수량은 1 이상이어야 합니다")
    Integer usageLimit;
}
