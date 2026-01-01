package com.jy.shoppy.domain.review.controller;

import com.jy.shoppy.domain.review.dto.ReviewResponse;
import com.jy.shoppy.domain.review.service.AdminReviewService;
import com.jy.shoppy.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Review", description = "관리자 리뷰 관리 API")
@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    /**
     * 모든 리뷰 조회 (페이징)
     */
    @GetMapping
    @Operation(
            summary = "[관리자] 전체 리뷰 조회",
            description = "모든 리뷰를 페이징으로 조회합니다."
    )
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getAllReviews(
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(
                ApiResponse.success(adminReviewService.getAllReviews(pageable), HttpStatus.OK)
        );
    }

    /**
     * 특정 리뷰 조회
     */
    @GetMapping("/{reviewId}")
    @Operation(
            summary = "[관리자] 리뷰 상세 조회",
            description = "특정 리뷰를 상세 조회합니다."
    )
    public ResponseEntity<ApiResponse<ReviewResponse>> getReview(
            @Parameter(description = "리뷰 ID") @PathVariable Long reviewId) {

        return ResponseEntity.ok(
                ApiResponse.success(adminReviewService.getReview(reviewId), HttpStatus.OK)
        );
    }

    /**
     * 리뷰 삭제 (관리자)
     */
    @DeleteMapping("/{reviewId}")
    @Operation(
            summary = "[관리자] 리뷰 삭제",
            description = "부적절한 리뷰를 삭제합니다. 관리자는 모든 리뷰를 삭제할 수 있습니다."
    )
    public ResponseEntity<ApiResponse<String>> deleteReview(
            @Parameter(description = "리뷰 ID") @PathVariable Long reviewId) {

        adminReviewService.deleteReview(reviewId);
        return ResponseEntity.ok(
                ApiResponse.success("리뷰가 삭제되었습니다.", HttpStatus.OK)
        );
    }

    /**
     * 상품별 리뷰 조회
     */
    @GetMapping("/product/{productId}")
    @Operation(
            summary = "[관리자] 상품별 리뷰 조회",
            description = "특정 상품의 모든 리뷰를 조회합니다."
    )
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getProductReviews(
            @Parameter(description = "상품 ID") @PathVariable Long productId,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        adminReviewService.getProductReviews(productId, pageable),
                        HttpStatus.OK
                )
        );
    }

    /**
     * 사용자별 리뷰 조회
     */
    @GetMapping("/user/{userId}")
    @Operation(
            summary = "[관리자] 사용자별 리뷰 조회",
            description = "특정 사용자가 작성한 모든 리뷰를 조회합니다."
    )
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getUserReviews(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        adminReviewService.getUserReviews(userId, pageable),
                        HttpStatus.OK
                )
        );
    }
}