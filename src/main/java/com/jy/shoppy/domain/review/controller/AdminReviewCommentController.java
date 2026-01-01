package com.jy.shoppy.domain.review.controller;

import com.jy.shoppy.domain.review.dto.CommentResponse;
import com.jy.shoppy.domain.review.service.AdminReviewCommentService;
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

@Tag(name = "Admin Review Comment", description = "관리자 댓글 관리 API")
@RestController
@RequestMapping("/api/admin/reviews/comments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AdminReviewCommentController {
    private final AdminReviewCommentService adminCommentService;

    /**
     * 전체 댓글 조회
     */
    @GetMapping
    @Operation(
            summary = "[관리자] 전체 댓글 조회",
            description = "모든 리뷰 댓글을 페이징으로 조회합니다."
    )
    public ResponseEntity<ApiResponse<Page<CommentResponse>>> getAllComments(
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(
                ApiResponse.success(adminCommentService.getAllComments(pageable), HttpStatus.OK)
        );
    }

    /**
     * 특정 댓글 조회
     */
    @GetMapping("/{commentId}")
    @Operation(
            summary = "[관리자] 댓글 상세 조회",
            description = "특정 댓글을 상세 조회합니다."
    )
    public ResponseEntity<ApiResponse<CommentResponse>> getComment(
            @Parameter(description = "댓글 ID") @PathVariable Long commentId) {

        return ResponseEntity.ok(
                ApiResponse.success(adminCommentService.getComment(commentId), HttpStatus.OK)
        );
    }

    /**
     * ✅ 댓글 삭제 (관리자 - 모든 댓글 삭제 가능)
     */
    @DeleteMapping("/{commentId}")
    @Operation(
            summary = "[관리자] 댓글 삭제",
            description = "부적절한 댓글을 삭제합니다. 관리자는 모든 댓글을 삭제할 수 있습니다."
    )
    public ResponseEntity<ApiResponse<String>> deleteComment(
            @Parameter(description = "댓글 ID") @PathVariable Long commentId) {

        adminCommentService.deleteComment(commentId);
        return ResponseEntity.ok(
                ApiResponse.success("댓글이 삭제되었습니다.", HttpStatus.OK)
        );
    }

    /**
     * 특정 리뷰의 댓글 조회
     */
    @GetMapping("/review/{reviewId}")
    @Operation(
            summary = "[관리자] 리뷰별 댓글 조회",
            description = "특정 리뷰의 모든 댓글을 조회합니다."
    )
    public ResponseEntity<ApiResponse<Page<CommentResponse>>> getReviewComments(
            @Parameter(description = "리뷰 ID") @PathVariable Long reviewId,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        adminCommentService.getReviewComments(reviewId, pageable),
                        HttpStatus.OK
                )
        );
    }

    /**
     * 특정 사용자의 댓글 조회
     */
    @GetMapping("/user/{userId}")
    @Operation(
            summary = "[관리자] 사용자별 댓글 조회",
            description = "특정 사용자가 작성한 모든 댓글을 조회합니다."
    )
    public ResponseEntity<ApiResponse<Page<CommentResponse>>> getUserComments(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        adminCommentService.getUserComments(userId, pageable),
                        HttpStatus.OK
                )
        );
    }
}