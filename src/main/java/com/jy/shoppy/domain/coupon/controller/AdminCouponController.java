package com.jy.shoppy.domain.coupon.controller;

import com.jy.shoppy.domain.coupon.dto.*;
import com.jy.shoppy.domain.coupon.entity.type.CouponSortType;
import com.jy.shoppy.domain.coupon.service.CouponService;
import com.jy.shoppy.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin Coupon", description = "관리자 쿠폰 생성/발급/관리 API")
@RestController
@RequestMapping("/api/admin/coupons")
@RequiredArgsConstructor
public class AdminCouponController {
    private final CouponService couponService;

    @Operation(
            summary = "쿠폰 생성 API",
            description = "관리자가 쿠폰을 생성합니다."
    )
    @PostMapping("/templates")
    public ResponseEntity<ApiResponse<CouponResponse>> createTemplate(
            @Valid @RequestBody CreateCouponRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(couponService.create(req), HttpStatus.CREATED));
    }

    @Operation(
            summary = "쿠폰 적용 가능 상품 추가",
            description = "쿠폰에 적용 가능한 상품을 추가합니다. (applicationType이 PRODUCT인 경우만)"
    )
    @PostMapping("/{couponId}/products")
    public ResponseEntity<ApiResponse<String>> addProducts(
            @PathVariable Long couponId,
            @Valid @RequestBody AddCouponProductsRequest request
    ) {
        couponService.addProducts(couponId, request);
        return ResponseEntity.ok(
                ApiResponse.success("쿠폰 적용 상품이 추가되었습니다.", HttpStatus.OK)
        );
    }

    @Operation(
            summary = "쿠폰 적용 가능 상품 조회",
            description = "쿠폰에 적용 가능한 상품 목록을 조회합니다."
    )
    @GetMapping("/{couponId}/products")
    public ResponseEntity<ApiResponse<List<CouponProductResponse>>> getCouponProducts(
            @PathVariable Long couponId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(couponService.findCouponProducts(couponId), HttpStatus.OK)
        );
    }

    @Operation(
            summary = "쿠폰 적용 가능 상품 삭제",
            description = "쿠폰에서 특정 상품을 제거합니다."
    )
    @DeleteMapping("/{couponId}/products/{productId}")
    public ResponseEntity<ApiResponse<String>> removeProduct(
            @PathVariable Long couponId,
            @PathVariable Long productId
    ) {
        couponService.removeProduct(couponId, productId);
        return ResponseEntity.ok(
                ApiResponse.success("쿠폰 적용 상품이 삭제되었습니다.", HttpStatus.OK)
        );
    }

    @Operation(
            summary = "쿠폰 적용 가능 카테고리 추가",
            description = "쿠폰에 적용 가능한 카테고리를 추가합니다. (applicationType이 CATEGORY인 경우만)"
    )
    @PostMapping("/{couponId}/categories")
    public ResponseEntity<ApiResponse<String>> addCategories(
            @PathVariable Long couponId,
            @Valid @RequestBody AddCouponCategoriesRequest request
    ) {
        couponService.addCategories(couponId, request);
        return ResponseEntity.ok(
                ApiResponse.success("쿠폰 적용 카테고리가 추가되었습니다.", HttpStatus.OK)
        );
    }

    @Operation(
            summary = "쿠폰 적용 가능 카테고리 조회",
            description = "쿠폰에 적용 가능한 카테고리 목록을 조회합니다."
    )
    @GetMapping("/{couponId}/categories")
    public ResponseEntity<ApiResponse<List<CouponCategoryResponse>>> getCouponCategories(
            @PathVariable Long couponId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(couponService.findCouponCategories(couponId), HttpStatus.OK)
        );
    }

    @Operation(
            summary = "쿠폰 적용 가능 카테고리 삭제",
            description = "쿠폰에서 특정 카테고리를 제거합니다."
    )
    @DeleteMapping("/{couponId}/categories/{categoryId}")
    public ResponseEntity<ApiResponse<String>> removeCategory(
            @PathVariable Long couponId,
            @PathVariable Long categoryId
    ) {
        couponService.removeCategory(couponId, categoryId);
        return ResponseEntity.ok(
                ApiResponse.success("쿠폰 적용 카테고리가 삭제되었습니다.", HttpStatus.OK)
        );
    }

    @Operation(
            summary = "쿠폰 대량 발급",
            description = "생성된 쿠폰을 대량으로 발급합니다. 요청된 개수만큼 고유한 쿠폰 코드를 생성합니다."
    )
    @PostMapping("/{couponId}/issue")
    public ResponseEntity<ApiResponse<IssueCouponResponse>> issueCoupons(
            @PathVariable Long couponId,
            @Valid @RequestBody IssueCouponRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(couponService.issueCoupons(couponId, request), HttpStatus.CREATED));
    }

    @Operation(summary = "쿠폰 삭제", description = "쿠폰을 삭제합니다.")
    @DeleteMapping("/{couponId}")
    public ResponseEntity<ApiResponse<String>> deleteCoupon(
            @PathVariable Long couponId
    ) {
        couponService.delete(couponId);
        return ResponseEntity.ok(
                ApiResponse.success("리뷰가 삭제되었습니다.", HttpStatus.OK)
        );
    }

    @Operation(
            summary = "쿠폰 수정 API",
            description = "관리자가 쿠폰을 수정합니다."
    )
    @PutMapping("/{couponId}")
    public ResponseEntity<ApiResponse<CouponResponse>> updateTemplate(
            @PathVariable Long couponId,
            @Valid @RequestBody UpdateCouponRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(couponService.update(couponId, request), HttpStatus.OK));
    }

    @Operation(
            summary = "쿠폰 단건 조회",
            description = "쿠폰 ID로 조회합니다."
    )
    @GetMapping("/{couponId}")
    public ResponseEntity<ApiResponse<CouponResponse>> getCoupon(
            @PathVariable Long couponId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(couponService.findById(couponId), HttpStatus.OK)
        );
    }

    @Operation(
            summary = "쿠폰 전체 조회 (정렬)",
            description = "모든 쿠폰을 정렬하여 조회합니다. (최신순/할인순/만료임박순)"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getAllCoupons(
            @RequestParam(required = false, defaultValue = "LATEST") CouponSortType sortType
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(couponService.findAllSorted(sortType), HttpStatus.OK)
        );
    }
}
