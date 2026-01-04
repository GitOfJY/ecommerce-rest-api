package com.jy.shoppy.domain.coupon.controller;

import com.jy.shoppy.domain.auth.dto.Account;
import com.jy.shoppy.domain.coupon.dto.CouponApplicableProductsResponse;
import com.jy.shoppy.domain.coupon.dto.RegisterCouponResponse;
import com.jy.shoppy.domain.coupon.dto.UserCouponResponse;
import com.jy.shoppy.domain.coupon.entity.type.CouponSortType;
import com.jy.shoppy.domain.coupon.service.CouponService;
import com.jy.shoppy.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Coupon", description = "사용자 쿠폰 등록/조회 API")
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

    @Operation(
            summary = "쿠폰 적용 가능 상품/카테고리 조회 (등록 전)",
            description = "쿠폰 코드로 어떤 상품/카테고리에 적용 가능한지 조회합니다. (쿠폰 등록 전)"
    )
    @GetMapping("/{couponCode}/applicable")
    public ResponseEntity<ApiResponse<CouponApplicableProductsResponse>> getApplicableProducts(
            @PathVariable String couponCode
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(couponService.findApplicableProductsByCouponCode(couponCode), HttpStatus.OK)
        );
    }

    @Operation(
            summary = "내 쿠폰 적용 가능 상품/카테고리 조회",
            description = "등록한 쿠폰이 어떤 상품/카테고리에 적용 가능한지 조회합니다."
    )
    @GetMapping("/my/{couponUserId}/applicable")
    public ResponseEntity<ApiResponse<CouponApplicableProductsResponse>> getMyApplicableProducts(
            @PathVariable Long couponUserId,
            @AuthenticationPrincipal Account account
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(couponService.findMyApplicableProducts(couponUserId, account), HttpStatus.OK)
        );
    }

    @Operation(
            summary = "특정 상품에 적용 가능한 내 쿠폰 조회",
            description = "장바구니/주문 시 특정 상품에 사용할 수 있는 내 쿠폰 목록을 조회합니다."
    )
    @GetMapping("/applicable-for-product")
    public ResponseEntity<ApiResponse<List<UserCouponResponse>>> getApplicableCouponsForProduct(
            @RequestParam Long productId,
            @AuthenticationPrincipal Account account
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(couponService.findApplicableCouponsForProduct(productId, account), HttpStatus.OK)
        );
    }
}
