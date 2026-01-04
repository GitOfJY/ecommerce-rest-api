package com.jy.shoppy.domain.coupon.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddCouponCategoriesRequest {
    @NotEmpty(message = "카테고리 ID 목록은 필수입니다")
    List<Long> categoryIds;
}
