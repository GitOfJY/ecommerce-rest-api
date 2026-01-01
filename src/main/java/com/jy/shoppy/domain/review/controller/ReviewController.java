package com.jy.shoppy.domain.review.controller;

import com.jy.shoppy.domain.auth.dto.Account;
import com.jy.shoppy.domain.review.dto.*;
import com.jy.shoppy.domain.review.service.ReviewService;
import com.jy.shoppy.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Review", description = "리뷰 API")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @Operation(
            summary = "리뷰 등록 API",
            description = "새로운 리뷰를 등록합니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<CreateReviewResponse>> create(
            @RequestBody @Valid CreateReviewRequest req,
            @AuthenticationPrincipal Account account) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(reviewService.create(req, account), HttpStatus.CREATED));
    }

    @Operation(
            summary = "리뷰 수정 API",
            description = "리뷰를 수정합니다."
    )
    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> update(
            @PathVariable Long reviewId,
            @RequestBody @Valid UpdateReviewRequest req,
            @AuthenticationPrincipal Account account) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.update(reviewId, req, account), HttpStatus.OK));
    }


    @Operation(
            summary = "리뷰 삭제 API",
            description = "리뷰를 삭제합니다."
    )
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<String>> delete(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal Account account) {
        reviewService.delete(reviewId, account);
        return ResponseEntity.ok(
                ApiResponse.success("리뷰가 삭제되었습니다.", HttpStatus.OK)
        );
    }

    @GetMapping("/reviewable")
    @Operation(
            summary = "리뷰 작성 가능한 상품 목록 조회",
            description = "구매 확정(COMPLETED)되었지만 아직 리뷰를 작성하지 않은 상품 목록을 조회합니다."
    )
    public ResponseEntity<ApiResponse<List<ReviewableProductResponse>>> getReviewableProducts(
            @AuthenticationPrincipal Account account) {
        return ResponseEntity.ok(
                ApiResponse.success(reviewService.getReviewableProducts(account), HttpStatus.OK)
        );
    }

    @Operation(
            summary = "내 리뷰 목록 조회",
            description = "내가 작성한 모든 리뷰를 조회합니다."
    )
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getMyReviews(
            @AuthenticationPrincipal Account account) {
        return ResponseEntity.ok(
                ApiResponse.success(reviewService.getMyReviews(account), HttpStatus.OK)
        );
    }

    @GetMapping("/product/{productId}")
    @Operation(
            summary = "상품 리뷰 목록 조회 (로그인 불필요)",
            description = "특정 상품의 리뷰를 조회합니다. 정렬 및 평점 필터링 가능"
    )
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getProductReviews(
            @Parameter(description = "상품 ID") @PathVariable Long productId,
            @Parameter(description = "최소 평점 (1~5)") @RequestParam(required = false) Integer minRating,
            @Parameter(description = "정렬 (latest, rating_high, rating_low, helpful)")
            @RequestParam(defaultValue = "latest") String sort,
            @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        reviewService.getProductReviews(productId, minRating, sort, pageable),
                        HttpStatus.OK
                )
        );
    }
}
