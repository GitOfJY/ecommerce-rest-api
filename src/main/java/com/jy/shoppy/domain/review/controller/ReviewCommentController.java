package com.jy.shoppy.domain.review.controller;

import com.jy.shoppy.domain.auth.dto.Account;
import com.jy.shoppy.domain.review.dto.CommentResponse;
import com.jy.shoppy.domain.review.dto.CreateCommentRequest;
import com.jy.shoppy.domain.review.dto.UpdateCommentRequest;
import com.jy.shoppy.domain.review.service.ReviewCommentService;
import com.jy.shoppy.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Review Comment", description = "리뷰 댓글 API")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewCommentController {
    private final ReviewCommentService commentService;

    @PostMapping("/{reviewId}/comments")
    @Operation(
            summary = "리뷰 댓글 작성",
            description = "리뷰에 댓글을 작성합니다."
    )
    public ResponseEntity<ApiResponse<CommentResponse>> create(
            @Parameter(description = "리뷰 ID") @PathVariable Long reviewId,
            @RequestBody @Valid CreateCommentRequest req,
            @AuthenticationPrincipal Account account) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        commentService.create(reviewId, req, account),
                        HttpStatus.CREATED
                ));
    }

    @PutMapping("/comments/{commentId}")
    @Operation(
            summary = "댓글 수정",
            description = "작성한 댓글을 수정합니다."
    )
    public ResponseEntity<ApiResponse<CommentResponse>> update(
            @Parameter @PathVariable Long commentId,
            @RequestBody @Valid UpdateCommentRequest req,
            @AuthenticationPrincipal Account account) {
        return ResponseEntity.ok(
                ApiResponse.success(commentService.update(commentId, req, account), HttpStatus.OK)
        );
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(
            summary = "댓글 삭제",
            description = "작성한 댓글을 삭제합니다."
    )
    public ResponseEntity<ApiResponse<String>> delete(
            @Parameter @PathVariable Long commentId,
            @AuthenticationPrincipal Account account) {
        commentService.delete(commentId, account);
        return ResponseEntity.ok(
                ApiResponse.success("댓글이 삭제되었습니다.", HttpStatus.OK)
        );
    }

    @GetMapping("/{reviewId}/comments")
    @Operation(
            summary = "리뷰 댓글 목록 조회",
            description = "특정 리뷰의 모든 댓글을 조회합니다. (로그인 불필요)"
    )
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(
            @Parameter @PathVariable Long reviewId) {
        return ResponseEntity.ok(
                ApiResponse.success(commentService.getComments(reviewId), HttpStatus.OK)
        );
    }

    @GetMapping("/comments/my")
    @Operation(
            summary = "내 댓글 목록 조회",
            description = "내가 작성한 모든 댓글을 조회합니다."
    )
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getMyComments(
            @AuthenticationPrincipal Account account) {
        return ResponseEntity.ok(
                ApiResponse.success(commentService.getMyComments(account), HttpStatus.OK)
        );
    }
}