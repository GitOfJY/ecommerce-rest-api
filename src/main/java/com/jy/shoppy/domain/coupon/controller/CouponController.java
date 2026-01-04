package com.jy.shoppy.domain.coupon.controller;

import com.jy.shoppy.domain.auth.dto.Account;
import com.jy.shoppy.domain.coupon.dto.CouponResponse;
import com.jy.shoppy.domain.coupon.dto.RegisterCouponResponse;
import com.jy.shoppy.domain.coupon.dto.UserCouponResponse;
import com.jy.shoppy.domain.coupon.entity.type.CouponSortType;
import com.jy.shoppy.domain.coupon.service.CouponService;
import com.jy.shoppy.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "사용자 쿠폰", description = "사용자 쿠폰 등록/조회 API")
@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;

    @Operation(
            summary = "쿠폰 등록 API",
            description = "사용자가 쿠폰을 등록합니다."
    )
    @PostMapping("/{couponCode}")
    public ResponseEntity<ApiResponse<RegisterCouponResponse>> register(
            @PathVariable String couponCode,
            @AuthenticationPrincipal Account account
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(couponService.registerCoupon(couponCode, account), HttpStatus.CREATED));
    }

    @Operation(
            summary = "내 쿠폰 전체 조회",
            description = "등록한 모든 쿠폰을 조회합니다. (최신순/할인순/만료임박순)"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserCouponResponse>>> getAll(
            @AuthenticationPrincipal Account account,
            @RequestParam(required = false, defaultValue = "LATEST") CouponSortType sortType
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(couponService.findAllByUserId(account, sortType), HttpStatus.OK)
        );
    }

    @Operation(
            summary = "내 쿠폰 조회 (사용 가능만)",
            description = "현재 사용 가능한 쿠폰만 조회합니다. (ISSUED 상태 + 만료 안됨)"
    )
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<UserCouponResponse>>> getAvailable(
            @AuthenticationPrincipal Account account,
            @RequestParam(required = false, defaultValue = "DISCOUNT_DESC") CouponSortType sortType
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(couponService.findAvailableByUserId(account, sortType), HttpStatus.OK)
        );
    }
}
