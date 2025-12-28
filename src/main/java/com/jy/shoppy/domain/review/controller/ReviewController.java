package com.jy.shoppy.domain.review.controller;

import com.jy.shoppy.domain.auth.dto.Account;
import com.jy.shoppy.domain.prodcut.dto.ProductResponse;
import com.jy.shoppy.domain.review.dto.CreateReviewRequest;
import com.jy.shoppy.domain.review.dto.CreateReviewResponse;
import com.jy.shoppy.domain.review.dto.ReviewResponse;
import com.jy.shoppy.domain.review.dto.UpdateReviewRequest;
import com.jy.shoppy.domain.review.service.ReviewService;
import com.jy.shoppy.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Review", description = "리뷰 API")
@RestController
@RequestMapping("/api/review")
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

    /*
    @Operation(
            summary = "리뷰 삭제 API",
            description = "리뷰를 삭제합니다."
    )
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Long>> delete(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal Account account) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.delete(reviewId, account)));
    }

        @Operation(
            summary = "작성 가능한 리뷰 목록 API",
            description = "작성 가능한 리뷰 목록을 조회합니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> getAll(
            @AuthenticationPrincipal Account account) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(reviewService.getAll(account), HttpStatus.CREATED));
    }
    */
}
